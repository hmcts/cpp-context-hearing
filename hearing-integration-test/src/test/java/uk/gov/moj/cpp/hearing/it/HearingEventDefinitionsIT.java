package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventDefinitionsAreAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenHearingEventDefinitionsAreRecorded;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
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
        andHearingEventDefinitionsAreAvailable(eventDefinitions);

        thenHearingEventDefinitionsAreRecorded(eventDefinitions);
    }
}
