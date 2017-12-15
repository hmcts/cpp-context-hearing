package uk.gov.moj.cpp.hearing.it;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventDefinitionsAreAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventLoggedPublicEventShouldNotBePublished;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingIsNotStarted;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andLogsAnotherEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andUserLogsAnEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenHearingEventAlterableFlagIs;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenHearingEventDefinitionIsStillAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenHearingEventDefinitionsAreRecorded;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenHearingEventDefinitionsShouldProvideOptionToLogEventWithDefendantAndDefenceCouncil;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenHearingEventIsRecorded;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenItFailsForMissingEventTime;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenOnlySpecifiedHearingEventIsRecorded;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenTheEventsShouldBeListedInTheSpecifiedOrder;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenHearingEventDefinitionsAreUpdated;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenUserAttemptsToLogAHearingEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenUserCorrectsTheTimeOfTheHearingEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenUserLogsAnEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.whenHearingHasDefendantsWithDefenceCounsels;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingDataFactory.defenceCounsel;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEndedEvent;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithBothSequencedAndNonSequencedEvents;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithNotRegisteredSequenceTypeEvents;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithOnlyNonSequencedEvents;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithOnlySequencedEvents;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithPauseAndResumeEvents;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventWithMissingEventTime;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingPausedEvent;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingResumedEvent;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingStartedEvent;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.identifyDefendantEvent;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.mitigationEvent;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.jms.MessageConsumer;

import org.junit.Test;

import com.jayway.restassured.response.Response;

import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;
import uk.gov.moj.cpp.hearing.steps.data.DefenceCounselData;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;

public class HearingEventsIT extends AbstractIT {

    private final UUID userId = randomUUID();
    private final UUID hearingId = randomUUID();

    private final UUID defendantId = randomUUID();
    private final UUID defenceCounselId = randomUUID();
    private final UUID defendantId2 = randomUUID();
    private final UUID defenceCounselId2 = randomUUID();

    private final ZonedDateTime pastEventTime = PAST_ZONED_DATE_TIME.next();
    private final ZonedDateTime currentLastModifiedTime = ZonedDateTime.now();
    private static final String HEARING_LOGGED_PUBLIC_EVENT ="public.hearing.event-logged";

    @Test
    public void shouldNotRaiseHearingEventLoggedPublicEventWhenHearingNotInitiated() throws IOException, InterruptedException {
        final MessageConsumer messageConsumer = publicEvents.createConsumer(HEARING_LOGGED_PUBLIC_EVENT);

        givenAUserHasLoggedInAsACourtClerk(this.userId);
        andHearingEventDefinitionsAreAvailable(hearingEventDefinitionsWithOnlySequencedEvents());
        andHearingIsNotStarted(this.hearingId);

        final HearingEvent hearingStartedEvent = hearingStartedEvent(this.hearingId);
        whenUserLogsAnEvent(hearingStartedEvent);

        thenOnlySpecifiedHearingEventIsRecorded(hearingStartedEvent);

        andHearingEventLoggedPublicEventShouldNotBePublished(messageConsumer);
    }

    @Test
    public void shouldRejectAnHearingEventWhenEventTimeIsMissing() {
        givenAUserHasLoggedInAsACourtClerk(this.userId);
        andHearingEventDefinitionsAreAvailable(hearingEventDefinitionsWithOnlySequencedEvents());
        andHearingIsNotStarted(this.hearingId);

        final Response response = whenUserAttemptsToLogAHearingEvent(hearingEventWithMissingEventTime(this.hearingId));
        thenItFailsForMissingEventTime(response);
    }


    @Test
    public void shouldCorrectTimeOfASpecificHearingEventAndReturnHearingEventsInChronologicalOrderByEventTime() {
        givenAUserHasLoggedInAsACourtClerk(this.userId);
        andHearingEventDefinitionsAreAvailable(hearingEventDefinitionsWithOnlySequencedEvents());

        final HearingEvent hearingStartedEvent = hearingStartedEvent(this.hearingId);
        andUserLogsAnEvent(hearingStartedEvent);

        final HearingEvent identifyDefendantEvent = identifyDefendantEvent(this.hearingId);
        andLogsAnotherEvent(identifyDefendantEvent);

        final UUID newHearingEventId = randomUUID();
        whenUserCorrectsTheTimeOfTheHearingEvent(identifyDefendantEvent, this.pastEventTime, this.currentLastModifiedTime, newHearingEventId);

        final HearingEvent updatedIdentifyDefendantEvent = identifyDefendantEvent.builder()
                .withId(newHearingEventId)
                .withEventTime(this.pastEventTime)
                .withLastModifiedTime(this.currentLastModifiedTime)
                .build();
        thenTheEventsShouldBeListedInTheSpecifiedOrder(this.hearingId, asList(updatedIdentifyDefendantEvent, hearingStartedEvent));
    }

    @Test
    public void shouldBeAbleToGenerateAndRecordMitigationEventsThatRequireDefendantAndDefenceCounselAlongWithGroupLabelAndActionLabelExtension() {
        givenAUserHasLoggedInAsACourtClerk(this.userId);

        andHearingEventDefinitionsAreAvailable(hearingEventDefinitionsWithOnlySequencedEvents());

        final List<DefenceCounselData> defenceCounsels = newArrayList(defenceCounsel(this.defenceCounselId, this.defendantId),
                defenceCounsel(this.defenceCounselId2, this.defendantId2));
        whenHearingHasDefendantsWithDefenceCounsels(this.hearingId, defenceCounsels);

        thenHearingEventDefinitionsShouldProvideOptionToLogEventWithDefendantAndDefenceCouncil(this.hearingId, defenceCounsels);

        final DefenceCounselData randomDefenceCounsel = values(defenceCounsels).next();
        final HearingEvent mitigationEvent = mitigationEvent(this.hearingId, randomDefenceCounsel);
        whenUserLogsAnEvent(mitigationEvent);

        thenHearingEventIsRecorded(mitigationEvent);
    }

    @Test
    public void shouldRecordAndReturnBothSequencedAndNonSequencedHearingEventDefinitions() {
        givenAUserHasLoggedInAsACourtClerk(this.userId);

        final HearingEventDefinitionData eventDefinitions = hearingEventDefinitionsWithBothSequencedAndNonSequencedEvents();
        andHearingEventDefinitionsAreAvailable(eventDefinitions);

        thenHearingEventDefinitionsAreRecorded(this.hearingId, eventDefinitions);
    }

    @Test
    public void shouldRecordAndReturnOnlyNonSequencedHearingEventDefinitions() {
        givenAUserHasLoggedInAsACourtClerk(this.userId);

        final HearingEventDefinitionData eventDefinitions = hearingEventDefinitionsWithOnlyNonSequencedEvents();
        andHearingEventDefinitionsAreAvailable(eventDefinitions);

        thenHearingEventDefinitionsAreRecorded(this.hearingId, eventDefinitions);
    }

    @Test
    public void shouldRecordAndReturnPauseAndResumeEventsInHearingEventDefinitions() {
        givenAUserHasLoggedInAsACourtClerk(this.userId);

        final HearingEventDefinitionData eventDefinitions = hearingEventDefinitionsWithPauseAndResumeEvents();
        andHearingEventDefinitionsAreAvailable(eventDefinitions);

        thenHearingEventDefinitionsAreRecorded(this.hearingId, eventDefinitions);
    }

    @Test
    public void shouldRecordAndReturnNotRegisteredSequenceTypeEventsWhichAppearAfterTheRegisteredSequenceTypeEventsInHearingEventDefinitions() {
        givenAUserHasLoggedInAsACourtClerk(this.userId);

        final HearingEventDefinitionData eventDefinitions = hearingEventDefinitionsWithNotRegisteredSequenceTypeEvents();
        andHearingEventDefinitionsAreAvailable(eventDefinitions);

        thenHearingEventDefinitionsAreRecorded(this.hearingId, eventDefinitions);
    }

    @Test
    public void shouldIndicateIfALoggedHearingEventIsAlterable() {
        givenAUserHasLoggedInAsACourtClerk(this.userId);

        andHearingEventDefinitionsAreAvailable(hearingEventDefinitionsWithPauseAndResumeEvents());

        final HearingEvent hearingStartedEvent = hearingStartedEvent(this.hearingId);
        final HearingEvent hearingPausedEvent = hearingPausedEvent(this.hearingId);
        final HearingEvent hearingResumedEvent = hearingResumedEvent(this.hearingId);
        final HearingEvent hearingEndedEvent = hearingEndedEvent(this.hearingId);
        andUserLogsAnEvent(hearingStartedEvent);
        andUserLogsAnEvent(hearingPausedEvent);
        andUserLogsAnEvent(hearingResumedEvent);
        andUserLogsAnEvent(hearingEndedEvent);

        thenHearingEventAlterableFlagIs(hearingStartedEvent, false);
        thenHearingEventAlterableFlagIs(hearingPausedEvent, false);
        thenHearingEventAlterableFlagIs(hearingResumedEvent, false);
        thenHearingEventAlterableFlagIs(hearingEndedEvent, false);

        final HearingEvent identifyDefendantEvent = identifyDefendantEvent(this.hearingId);
        andUserLogsAnEvent(identifyDefendantEvent);

        thenHearingEventAlterableFlagIs(identifyDefendantEvent, true);
    }

    @Test
    public void shouldBeAbleToQueryForADeletedHearingEventDefinition() {
        givenAUserHasLoggedInAsACourtClerk(this.userId);

        final HearingEventDefinitionData initialHearingEventDefinitions = hearingEventDefinitionsWithOnlySequencedEvents();
        andHearingEventDefinitionsAreAvailable(initialHearingEventDefinitions);

        whenHearingEventDefinitionsAreUpdated(hearingEventDefinitionsWithPauseAndResumeEvents());

        thenHearingEventDefinitionIsStillAvailable(values(initialHearingEventDefinitions.getEventDefinitions()).next());
    }


}
