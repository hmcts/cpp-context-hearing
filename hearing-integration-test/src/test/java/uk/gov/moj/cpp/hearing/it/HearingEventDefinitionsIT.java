package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventDefinitionsAreAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenHearingEventDefinitionsAreRecorded;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithBothSequencedAndNonSequencedEvents;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithNotRegisteredSequenceTypeEvents;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithOnlyNonSequencedEvents;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithOnlySequencedEvents;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithPauseAndResumeEvents;

public class HearingEventDefinitionsIT extends AbstractIT {
    
    private final UUID userId = randomUUID();

    @Test
    public void shouldRecordAndReturnOnlyNonSequencedHearingEventDefinitions() {

        givenAUserHasLoggedInAsACourtClerk(this.userId);

        final HearingEventDefinitionData eventDefinitions = hearingEventDefinitionsWithOnlyNonSequencedEvents();
        andHearingEventDefinitionsAreAvailable(eventDefinitions);

        thenHearingEventDefinitionsAreRecorded(eventDefinitions);
    }

    @Test
    public void shouldRecordAndReturnPauseAndResumeEventsInHearingEventDefinitions() {

        givenAUserHasLoggedInAsACourtClerk(this.userId);

        final HearingEventDefinitionData eventDefinitions = hearingEventDefinitionsWithPauseAndResumeEvents();
        andHearingEventDefinitionsAreAvailable(eventDefinitions);

        thenHearingEventDefinitionsAreRecorded(eventDefinitions);
    }

    @Test
    public void shouldRecordAndReturnNotRegisteredSequenceTypeEventsWhichAppearAfterTheRegisteredSequenceTypeEventsInHearingEventDefinitions() {

        givenAUserHasLoggedInAsACourtClerk(this.userId);
        final HearingEventDefinitionData eventDefinitions = hearingEventDefinitionsWithNotRegisteredSequenceTypeEvents();
        andHearingEventDefinitionsAreAvailable(eventDefinitions);

        thenHearingEventDefinitionsAreRecorded(eventDefinitions);
    }

    @Test
    public void shouldRecordAndReturnBothSequencedAndNonSequencedHearingEventDefinitions() {

        givenAUserHasLoggedInAsACourtClerk(this.userId);
        final HearingEventDefinitionData eventDefinitions = hearingEventDefinitionsWithBothSequencedAndNonSequencedEvents();
        andHearingEventDefinitionsAreAvailable(eventDefinitions);

        thenHearingEventDefinitionsAreRecorded(eventDefinitions);
    }
}
