package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.AssociatedPerson;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.associatedPerson;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.second;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

@SuppressWarnings({"squid:S2699"})
public class AddDefendantIT extends AbstractIT {

    @Test
    public void shouldAddNewDefendant() throws Exception {

        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        HearingDay hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0);
        hearingDay.setSittingDay(ZonedDateTime.now().plusDays(1));
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, initiateHearingCommand));

        final Defendant addNewDefendant = standardInitiateHearingTemplate().getHearing().getProsecutionCases().get(0).getDefendants().get(0);
        addNewDefendant.setAssociatedPersons(asList(associatedPerson(defaultArguments()).build()));
        addNewDefendant.setProsecutionCaseId(hearingOne.getFirstDefendantForFirstCase().getProsecutionCaseId());
        UseCases.addDefendant(addNewDefendant);
        final AssociatedPerson associatedPerson = addNewDefendant.getAssociatedPersons().get(0);
        final Person person = associatedPerson.getPerson();
        final Address address = person.getAddress();
        final ContactNumber contact = person.getContact();
        hearingOne.getFirstCase().getDefendants().add(addNewDefendant);

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))

                                .with(ProsecutionCase::getDefendants, second(isBean(Defendant.class)

                                ))))));


    }
}