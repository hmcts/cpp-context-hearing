package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;

import javax.json.JsonObject;
import java.util.UUID;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventDefinitionsAreAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenHearingEventDefinitionIsStillAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenHearingEventDefinitionsAreRecorded;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenHearingEventDefinitionsAreUpdated;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.andHearingHasBeenConfirmed;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.andProgressionCaseDetailsAreAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithBothSequencedAndNonSequencedEvents;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithNotRegisteredSequenceTypeEvents;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithOnlyNonSequencedEvents;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithOnlySequencedEvents;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithPauseAndResumeEvents;
import static uk.gov.moj.cpp.hearing.steps.data.factory.ProgressionDataFactory.hearingConfirmedFor;

public class HearingEventDefinitionsIT extends AbstractIT {


    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_URN = "urn";

    private final UUID userId = randomUUID();
    private UUID hearingId = randomUUID();


    @Test
    public void shouldRecordAndReturnOnlyNonSequencedHearingEventDefinitions() {
        final JsonObject hearingConfirmed = hearingConfirmedFor(hearingId);

        givenAUserHasLoggedInAsACourtClerk(this.userId);
        final HearingEventDefinitionData eventDefinitions = hearingEventDefinitionsWithOnlyNonSequencedEvents();
        andHearingEventDefinitionsAreAvailable(eventDefinitions);
        andHearingHasBeenConfirmed(hearingConfirmed);
        andProgressionCaseDetailsAreAvailable(fromString(hearingConfirmed.getString(FIELD_CASE_ID)), hearingConfirmed.getString(FIELD_URN));

        thenHearingEventDefinitionsAreRecorded(this.hearingId, eventDefinitions);
    }

    @Test
    public void shouldBeAbleToQueryForADeletedHearingEventDefinition() {
        final JsonObject hearingConfirmed = hearingConfirmedFor(hearingId);

        givenAUserHasLoggedInAsACourtClerk(this.userId);
        final HearingEventDefinitionData initialHearingEventDefinitions = hearingEventDefinitionsWithOnlySequencedEvents();
        andHearingEventDefinitionsAreAvailable(initialHearingEventDefinitions);
        andHearingHasBeenConfirmed(hearingConfirmed);
        andProgressionCaseDetailsAreAvailable(fromString(hearingConfirmed.getString(FIELD_CASE_ID)), hearingConfirmed.getString(FIELD_URN));

        whenHearingEventDefinitionsAreUpdated(hearingEventDefinitionsWithPauseAndResumeEvents());

        thenHearingEventDefinitionIsStillAvailable(values(initialHearingEventDefinitions.getEventDefinitions()).next());
    }

    @Test
    public void shouldRecordAndReturnPauseAndResumeEventsInHearingEventDefinitions() {
        final JsonObject hearingConfirmed = hearingConfirmedFor(hearingId);

        givenAUserHasLoggedInAsACourtClerk(this.userId);
        final HearingEventDefinitionData eventDefinitions = hearingEventDefinitionsWithPauseAndResumeEvents();
        andHearingEventDefinitionsAreAvailable(eventDefinitions);
        andHearingHasBeenConfirmed(hearingConfirmed);
        andProgressionCaseDetailsAreAvailable(fromString(hearingConfirmed.getString(FIELD_CASE_ID)), hearingConfirmed.getString(FIELD_URN));

        thenHearingEventDefinitionsAreRecorded(this.hearingId, eventDefinitions);
    }



    @Test
    public void shouldRecordAndReturnNotRegisteredSequenceTypeEventsWhichAppearAfterTheRegisteredSequenceTypeEventsInHearingEventDefinitions() {
        final JsonObject hearingConfirmed = hearingConfirmedFor(hearingId);

        givenAUserHasLoggedInAsACourtClerk(this.userId);
        final HearingEventDefinitionData eventDefinitions = hearingEventDefinitionsWithNotRegisteredSequenceTypeEvents();
        andHearingEventDefinitionsAreAvailable(eventDefinitions);
        andHearingHasBeenConfirmed(hearingConfirmed);
        andProgressionCaseDetailsAreAvailable(fromString(hearingConfirmed.getString(FIELD_CASE_ID)), hearingConfirmed.getString(FIELD_URN));

        thenHearingEventDefinitionsAreRecorded(this.hearingId, eventDefinitions);
    }

    @Test
    public void shouldRecordAndReturnBothSequencedAndNonSequencedHearingEventDefinitions() {
        final JsonObject hearingConfirmed = hearingConfirmedFor(hearingId);

        givenAUserHasLoggedInAsACourtClerk(this.userId);
        final HearingEventDefinitionData eventDefinitions = hearingEventDefinitionsWithBothSequencedAndNonSequencedEvents();
        andHearingEventDefinitionsAreAvailable(eventDefinitions);
        andHearingHasBeenConfirmed(hearingConfirmed);
        andProgressionCaseDetailsAreAvailable(fromString(hearingConfirmed.getString(FIELD_CASE_ID)), hearingConfirmed.getString(FIELD_URN));

        thenHearingEventDefinitionsAreRecorded(this.hearingId, eventDefinitions);
    }
}
