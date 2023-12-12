package uk.gov.moj.cpp.hearing.it;

import static org.hamcrest.core.Is.is;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddCompanyRepresentativeCommandTemplates.addCompanyRepresentativeCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateCompanyRepresentativeCommandTemplates.updateCompanyRepresentativeCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.second;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_MILLIS;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.CompanyRepresentative;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.hearing.courts.AddCompanyRepresentative;
import uk.gov.justice.hearing.courts.RemoveCompanyRepresentative;
import uk.gov.justice.hearing.courts.UpdateCompanyRepresentative;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeChangeIgnored;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import org.hamcrest.Matchers;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class CompanyRepresentativeIT extends AbstractIT {

    @Test
    public void shouldAddCompanyRepresentative() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        // Add the first company representative
        CompanyRepresentative firstCompanyRepresentative = createFirstCompanyRepresentative(hearingOne);

        // Add the second company representative
        createSecondCompanyRepresentative(hearingOne, firstCompanyRepresentative);

        // Duplicate company representative should not be added.
        // Public event 'public.hearing.company-representative-change-ignored' should be emitted in case of duplicate company representative is added.
        final Utilities.EventListener publicCompanyRepresentativeChangeIgnored = listenFor("public.hearing.company-representative-change-ignored", DEFAULT_POLL_TIMEOUT_IN_MILLIS)
                .withFilter(convertStringTo(CompanyRepresentativeChangeIgnored.class, isBean(CompanyRepresentativeChangeIgnored.class)
                        .with(CompanyRepresentativeChangeIgnored::getReason, Matchers.containsString("Provided company representative already exists"))));

        final AddCompanyRepresentative firstCompanyRepresentativeCommandAddedAgain = UseCases.addCompanyRepresentative(getRequestSpec(), hearingOne.getHearingId(),
                addCompanyRepresentativeCommandTemplate(hearingOne.getHearingId(), firstCompanyRepresentative)
        );

        publicCompanyRepresentativeChangeIgnored.waitFor();
    }


    @Test
    public void shouldUpdateCompanyRepresentative() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        // Add company representative
        CompanyRepresentative companyRepresentative = createFirstCompanyRepresentative(hearingOne);

        //Update the company representative
        companyRepresentative.setTitle("UpdatedTitle");
        companyRepresentative.setFirstName("UpdatedFirstName");
        companyRepresentative.setLastName("UpdatedLastName");
        companyRepresentative.setPosition("SECRETARY");

        final UpdateCompanyRepresentative updateCompanyRepresentativeCommand = UseCases.updateCompanyRepresentative(getRequestSpec(), hearingOne.getHearingId(),
                updateCompanyRepresentativeCommandTemplate(hearingOne.getHearingId(), companyRepresentative)
        );

        CompanyRepresentative updatedCompanyRepresentative = updateCompanyRepresentativeCommand.getCompanyRepresentative();

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, Matchers.is(hearingOne.getHearingId()))
                        .with(Hearing::getCompanyRepresentatives, first(isBean(CompanyRepresentative.class)
                                .with(CompanyRepresentative::getTitle, is(updatedCompanyRepresentative.getTitle()))
                                .with(CompanyRepresentative::getFirstName, is(updatedCompanyRepresentative.getFirstName()))
                                .with(CompanyRepresentative::getLastName, is(updatedCompanyRepresentative.getLastName()))
                                .with(CompanyRepresentative::getPosition, is(updatedCompanyRepresentative.getPosition()))))));
    }

    @Test
    public void shouldRemoveCompanyRepresentativeIfCompanyRepresentativeIsAvailableInHearing() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        // Add the first company representative
        CompanyRepresentative companyRepresentative = createFirstCompanyRepresentative(hearingOne);

        //remove company representative
        UseCases.removeCompanyRepresentative(getRequestSpec(), hearingOne.getHearingId(),
                new RemoveCompanyRepresentative(hearingOne.getHearingId(), companyRepresentative.getId())
        );

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, Matchers.is(hearingOne.getHearingId()))
                        .with(Hearing::getCompanyRepresentatives, Matchers.hasSize(is(0)))));

        // Remove same company representative which was removed already.
        // Public event 'public.hearing.company-representative-change-ignored' should be emitted in case of company representative which is already removed.
        final Utilities.EventListener publicCompanyRepresentativeChangeIgnored = listenFor("public.hearing.company-representative-change-ignored", DEFAULT_POLL_TIMEOUT_IN_MILLIS)
                .withFilter(convertStringTo(CompanyRepresentativeChangeIgnored.class, isBean(CompanyRepresentativeChangeIgnored.class)
                        .with(CompanyRepresentativeChangeIgnored::getReason, Matchers.containsString("Provided company representative does not exists"))));

        //remove company representative again
        UseCases.removeCompanyRepresentative(getRequestSpec(), hearingOne.getHearingId(),
                new RemoveCompanyRepresentative(hearingOne.getHearingId(), companyRepresentative.getId())
        );

        publicCompanyRepresentativeChangeIgnored.waitFor();

    }


    private static CompanyRepresentative createFirstCompanyRepresentative(final InitiateHearingCommandHelper hearingOne) {
        final AddCompanyRepresentative companyRepresentativeCommand = UseCases.addCompanyRepresentative(getRequestSpec(), hearingOne.getHearingId(),
                addCompanyRepresentativeCommandTemplate(hearingOne.getHearingId())
        );
        CompanyRepresentative companyRepresentative = companyRepresentativeCommand.getCompanyRepresentative();
        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, Matchers.is(hearingOne.getHearingId()))
                        .with(Hearing::getCompanyRepresentatives, first(isBean(CompanyRepresentative.class)
                                .with(CompanyRepresentative::getTitle, is(companyRepresentative.getTitle()))
                                .with(CompanyRepresentative::getFirstName, is(companyRepresentative.getFirstName()))
                                .with(CompanyRepresentative::getLastName, is(companyRepresentative.getLastName()))
                                .with(CompanyRepresentative::getPosition, is(companyRepresentative.getPosition()))))));
        return companyRepresentative;
    }

    private CompanyRepresentative createSecondCompanyRepresentative(final InitiateHearingCommandHelper hearingOne, final CompanyRepresentative firstCompanyRepresentative) {
        final AddCompanyRepresentative companyRepresentativeCommand = UseCases.addCompanyRepresentative(getRequestSpec(), hearingOne.getHearingId(),
                addCompanyRepresentativeCommandTemplate(hearingOne.getHearingId())
        );
        CompanyRepresentative secondCompanyRepresentative = companyRepresentativeCommand.getCompanyRepresentative();
        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, Matchers.is(hearingOne.getHearingId()))
                        .with(Hearing::getCompanyRepresentatives, first(isBean(CompanyRepresentative.class)
                                .with(CompanyRepresentative::getTitle, is(firstCompanyRepresentative.getTitle()))
                                .with(CompanyRepresentative::getFirstName, is(firstCompanyRepresentative.getFirstName()))
                                .with(CompanyRepresentative::getLastName, is(firstCompanyRepresentative.getLastName()))
                                .with(CompanyRepresentative::getPosition, is(firstCompanyRepresentative.getPosition()))))
                        .with(Hearing::getCompanyRepresentatives, second(isBean(CompanyRepresentative.class)
                                .with(CompanyRepresentative::getTitle, is(secondCompanyRepresentative.getTitle()))
                                .with(CompanyRepresentative::getFirstName, is(secondCompanyRepresentative.getFirstName()))
                                .with(CompanyRepresentative::getLastName, is(secondCompanyRepresentative.getLastName()))
                                .with(CompanyRepresentative::getPosition, is(secondCompanyRepresentative.getPosition()))))
                ));
        return secondCompanyRepresentative;
    }
}