package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.accessHearingEventsByGivenUser;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventDefinitionsAreAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenHearingEventDefinitionsAreRecorded;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsAGivenGroup;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithPauseAndResumeEvents;

import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;

import java.util.UUID;

import org.junit.Test;

public class HearingEventDefinitionsIT extends AbstractIT {

    private final UUID userId = randomUUID();

    @Test
    public void shouldRecordAndReturnPauseAndResumeEventsInHearingEventDefinitions() {

        givenAUserHasLoggedInAsACourtClerk(this.userId);

        final HearingEventDefinitionData eventDefinitions = hearingEventDefinitionsWithPauseAndResumeEvents();
        andHearingEventDefinitionsAreAvailable(eventDefinitions, userId);

        thenHearingEventDefinitionsAreRecorded(eventDefinitions);
    }

    @Test
    public void shouldReturnEventDefinitionsByDeputies() {

        UUID userIdNew = randomUUID();
        givenAUserHasLoggedInAsAGivenGroup(userIdNew, "Deputies");

        accessHearingEventsByGivenUser(userIdNew.toString());

    }

    @Test
    public void shouldReturnEventDefinitionsByDJMC() {

        UUID userIdNew = randomUUID();
        givenAUserHasLoggedInAsAGivenGroup(userIdNew, "DJMC");

        accessHearingEventsByGivenUser(userIdNew.toString());

    }

    @Test
    public void shouldReturnEventDefinitionsByJudge() {

        UUID userIdNew = randomUUID();
        givenAUserHasLoggedInAsAGivenGroup(userIdNew, "Judge");

        accessHearingEventsByGivenUser(userIdNew.toString());

    }
}
