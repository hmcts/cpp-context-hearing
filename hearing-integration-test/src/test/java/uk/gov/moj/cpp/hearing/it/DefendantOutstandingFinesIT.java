package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.Queries.pollForOutstandingFines;
import static uk.gov.moj.cpp.hearing.it.UseCases.addDefendant;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.associatedPerson;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubStagingEnforcementOutstandingFines;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.ZonedDateTime;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
@NotThreadSafe
class DefendantOutstandingFinesIT extends AbstractIT {

    @SuppressWarnings("squid:S2699")
    @Test
    void shouldBeAbleToQueryOutstandingFinesForDefendant() throws Exception {
        stubStagingEnforcementOutstandingFines();
        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        HearingDay hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0);
        hearingDay.setSittingDay(ZonedDateTime.now().plusDays(1));
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingCommand));

        final Defendant addNewDefendant = standardInitiateHearingTemplate().getHearing().getProsecutionCases().get(0).getDefendants().get(0);
        addNewDefendant.setAssociatedPersons(asList(associatedPerson(defaultArguments()).build()));
        addNewDefendant.setProsecutionCaseId(hearingOne.getFirstDefendantForFirstCase().getProsecutionCaseId());
        addDefendant(addNewDefendant);

        pollForOutstandingFines(addNewDefendant.getId().toString(),
                withJsonPath("$.outstandingFines.[0].defendantName", is("Abbie ARMSTRONG")),
                withJsonPath("$.outstandingFines.[0].dateOfBirth", is("1980-11-06"))
        );
    }
}