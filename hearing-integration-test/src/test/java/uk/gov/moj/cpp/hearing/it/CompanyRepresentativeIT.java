package uk.gov.moj.cpp.hearing.it;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.addCompanyRepresentative;
import static uk.gov.moj.cpp.hearing.it.UseCases.removeCompanyRepresentative;
import static uk.gov.moj.cpp.hearing.it.UseCases.updateCompanyRepresentative;
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
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class CompanyRepresentativeIT extends AbstractIT {

    @Test
    public void testCompanyRepresentative_shouldAddUpdateAndRemove() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        // Add the first company representative
        CompanyRepresentative firstCompanyRepresentative = createFirstCompanyRepresentative(hearingOne);

        // Add the second company representative
        createSecondCompanyRepresentative(hearingOne, firstCompanyRepresentative);

        // Duplicate company representative should not be added.
        // Public event 'public.hearing.company-representative-change-ignored' should be emitted in case of duplicate company representative is added.
        EventListener publicCompanyRepresentativeChangeIgnored = listenFor("public.hearing.company-representative-change-ignored", DEFAULT_POLL_TIMEOUT_IN_MILLIS)
                .withFilter(convertStringTo(CompanyRepresentativeChangeIgnored.class, isBean(CompanyRepresentativeChangeIgnored.class)
                        .with(CompanyRepresentativeChangeIgnored::getReason, containsString("Provided company representative already exists"))));

        final UUID hearingId = hearingOne.getHearingId();
        addCompanyRepresentative(getRequestSpec(), hearingId,
                addCompanyRepresentativeCommandTemplate(hearingId, firstCompanyRepresentative)
        );

        publicCompanyRepresentativeChangeIgnored.waitFor();

        // Update first company representative
        firstCompanyRepresentative.setTitle("UpdatedTitle");
        firstCompanyRepresentative.setFirstName("UpdatedFirstName");
        firstCompanyRepresentative.setLastName("UpdatedLastName");
        firstCompanyRepresentative.setPosition("SECRETARY");

        final UpdateCompanyRepresentative updateCompanyRepresentativeCommand = updateCompanyRepresentative(getRequestSpec(), hearingId,
                updateCompanyRepresentativeCommandTemplate(hearingId, firstCompanyRepresentative)
        );

        CompanyRepresentative updatedCompanyRepresentative = updateCompanyRepresentativeCommand.getCompanyRepresentative();

        getHearingPollForMatch(hearingId, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, Matchers.is(hearingId))
                        .with(Hearing::getCompanyRepresentatives, first(isBean(CompanyRepresentative.class)
                                .with(CompanyRepresentative::getTitle, is(updatedCompanyRepresentative.getTitle()))
                                .with(CompanyRepresentative::getFirstName, is(updatedCompanyRepresentative.getFirstName()))
                                .with(CompanyRepresentative::getLastName, is(updatedCompanyRepresentative.getLastName()))
                                .with(CompanyRepresentative::getPosition, is(updatedCompanyRepresentative.getPosition()))))));

        //Remove first company representative
        removeCompanyRepresentative(getRequestSpec(), hearingId,
                new RemoveCompanyRepresentative(hearingId, firstCompanyRepresentative.getId())
        );

        getHearingPollForMatch(hearingId, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, Matchers.is(hearingId))
                        .with(Hearing::getCompanyRepresentatives, Matchers.hasSize(is(1)))));

        // Remove same company representative which was removed already.
        //remove company representative
        removeCompanyRepresentative(getRequestSpec(), hearingId,
                new RemoveCompanyRepresentative(hearingId, firstCompanyRepresentative.getId())
        );

        getHearingPollForMatch(hearingId, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, Matchers.is(hearingId))
                        .with(Hearing::getCompanyRepresentatives, Matchers.hasSize(is(1)))));

        // Public event 'public.hearing.company-representative-change-ignored' should be emitted in case of company representative which is already removed.
        publicCompanyRepresentativeChangeIgnored = listenFor("public.hearing.company-representative-change-ignored", DEFAULT_POLL_TIMEOUT_IN_MILLIS)
                .withFilter(convertStringTo(CompanyRepresentativeChangeIgnored.class, isBean(CompanyRepresentativeChangeIgnored.class)
                        .with(CompanyRepresentativeChangeIgnored::getReason, containsString("Provided company representative does not exists"))));

        //remove company representative again
        removeCompanyRepresentative(getRequestSpec(), hearingId,
                new RemoveCompanyRepresentative(hearingId, firstCompanyRepresentative.getId())
        );

        publicCompanyRepresentativeChangeIgnored.waitFor();
    }

    private static CompanyRepresentative createFirstCompanyRepresentative(final InitiateHearingCommandHelper hearingOne) {
        final AddCompanyRepresentative companyRepresentativeCommand = addCompanyRepresentative(getRequestSpec(), hearingOne.getHearingId(),
                addCompanyRepresentativeCommandTemplate(hearingOne.getHearingId())
        );
        CompanyRepresentative companyRepresentative = companyRepresentativeCommand.getCompanyRepresentative();
        getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
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
        final AddCompanyRepresentative companyRepresentativeCommand = addCompanyRepresentative(getRequestSpec(), hearingOne.getHearingId(),
                addCompanyRepresentativeCommandTemplate(hearingOne.getHearingId())
        );
        CompanyRepresentative secondCompanyRepresentative = companyRepresentativeCommand.getCompanyRepresentative();
        getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
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