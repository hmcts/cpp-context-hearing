package uk.gov.moj.cpp.hearing.it;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventDefinitionsAreAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventLoggedPublicEventShouldBePublished;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventLoggedPublicEventShouldNotBePublished;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventTimeStampCorrectedPublicEventShouldBePublished;
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
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenTheHearingEventHasTheUpdatedEventTime;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenHearingEventDefinitionsAreUpdated;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenUserAttemptsToLogAHearingEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenUserCorrectsTheTimeOfTheHearingEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenUserLogsAnEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.andHearingHasBeenConfirmed;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.andHearingHasNotBeenConfirmed;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.andProgressionCaseDetailsAreAvailable;
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
import static uk.gov.moj.cpp.hearing.steps.data.factory.ProgressionDataFactory.hearingConfirmedFor;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;

import uk.gov.moj.cpp.hearing.command.initiate.Address;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.DefendantCase;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;
import uk.gov.moj.cpp.hearing.steps.data.DefenceCounselData;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.jms.MessageConsumer;
import javax.json.JsonObject;

import com.jayway.restassured.response.Response;
import org.junit.Test;

public class HearingEventsIT extends AbstractIT {

    private static final String PUBLIC_EVENT_HEARING_LOGGED = "public.hearing.event-logged";
    private static final String PUBLIC_EVENT_HEARING_TIMESTAMP_CORRECTED = "public.hearing.event-timestamp-corrected";

    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_URN = "urn";

    private final UUID userId = randomUUID();
    private UUID hearingId = randomUUID();

    private final UUID defendantId = randomUUID();
    private final UUID defenceCounselId = randomUUID();
    private final UUID defendantId2 = randomUUID();
    private final UUID defenceCounselId2 = randomUUID();

    private final ZonedDateTime pastEventTime = PAST_ZONED_DATE_TIME.next();
    private final ZonedDateTime currentLastModifiedTime = ZonedDateTime.now();

    @Test
    public void shouldBeAbleLogAHearingEventAndPublishWithEnrichedInformation() {
        final MessageConsumer messageConsumer = publicEvents.createConsumer(PUBLIC_EVENT_HEARING_LOGGED);
        final JsonObject hearingConfirmed = hearingConfirmedFor(hearingId);

        givenAUserHasLoggedInAsACourtClerk(userId);
        andHearingHasBeenConfirmed(hearingConfirmed);
        andProgressionCaseDetailsAreAvailable(fromString(hearingConfirmed.getString(FIELD_CASE_ID)), hearingConfirmed.getString(FIELD_URN));
        andHearingEventDefinitionsAreAvailable(hearingEventDefinitionsWithOnlySequencedEvents());

        final HearingEvent hearingStartedEvent = hearingStartedEvent(hearingId);
        whenUserLogsAnEvent(hearingStartedEvent);

        thenOnlySpecifiedHearingEventIsRecorded(hearingStartedEvent);

        andHearingEventLoggedPublicEventShouldBePublished(messageConsumer, hearingStartedEvent, hearingConfirmed);
    }

    @Test
    public void shouldBeAbleToCorrectTimeOfALoggedHearingEventAndPublishWithEnrichedInformation() {
        final MessageConsumer messageConsumer = publicEvents.createConsumer(PUBLIC_EVENT_HEARING_TIMESTAMP_CORRECTED);
        final JsonObject hearingConfirmed = hearingConfirmedFor(hearingId);

        givenAUserHasLoggedInAsACourtClerk(userId);
        andHearingHasBeenConfirmed(hearingConfirmed);
        andProgressionCaseDetailsAreAvailable(fromString(hearingConfirmed.getString(FIELD_CASE_ID)), hearingConfirmed.getString(FIELD_URN));
        andHearingEventDefinitionsAreAvailable(hearingEventDefinitionsWithOnlySequencedEvents());

        final HearingEvent hearingStartedEvent = hearingStartedEvent(hearingId);
        andUserLogsAnEvent(hearingStartedEvent);

        final UUID newHearingEventId = randomUUID();
        whenUserCorrectsTheTimeOfTheHearingEvent(hearingStartedEvent, pastEventTime, currentLastModifiedTime, newHearingEventId);

        thenTheHearingEventHasTheUpdatedEventTime(hearingStartedEvent, pastEventTime, currentLastModifiedTime, newHearingEventId);

        andHearingEventTimeStampCorrectedPublicEventShouldBePublished(
                messageConsumer, hearingStartedEvent, pastEventTime, currentLastModifiedTime,
                newHearingEventId, hearingConfirmed);
    }

    @Test
    public void shouldNotRaiseHearingEventLoggedPublicEventWhenHearingHasNotBeenConfirmed() {
        final MessageConsumer messageConsumer = publicEvents.createConsumer(PUBLIC_EVENT_HEARING_LOGGED);

        givenAUserHasLoggedInAsACourtClerk(this.userId);
        andHearingEventDefinitionsAreAvailable(hearingEventDefinitionsWithOnlySequencedEvents());
        andHearingHasNotBeenConfirmed(this.hearingId);

        final HearingEvent hearingStartedEvent = hearingStartedEvent(this.hearingId);
        whenUserLogsAnEvent(hearingStartedEvent);

        thenOnlySpecifiedHearingEventIsRecorded(hearingStartedEvent);

        andHearingEventLoggedPublicEventShouldNotBePublished(messageConsumer);
    }

    @Test
    public void shouldRejectAnHearingEventWhenEventTimeIsMissing() {
        final JsonObject hearingConfirmed = hearingConfirmedFor(hearingId);

        givenAUserHasLoggedInAsACourtClerk(this.userId);
        andHearingEventDefinitionsAreAvailable(hearingEventDefinitionsWithOnlySequencedEvents());
        andHearingHasBeenConfirmed(hearingConfirmed);
        andProgressionCaseDetailsAreAvailable(fromString(hearingConfirmed.getString(FIELD_CASE_ID)), hearingConfirmed.getString(FIELD_URN));

        final Response response = whenUserAttemptsToLogAHearingEvent(hearingEventWithMissingEventTime(this.hearingId));
        thenItFailsForMissingEventTime(response);
    }


    @Test
    public void shouldCorrectTimeOfASpecificHearingEventAndReturnHearingEventsInChronologicalOrderByEventTime() {
        final JsonObject hearingConfirmed = hearingConfirmedFor(hearingId);

        givenAUserHasLoggedInAsACourtClerk(this.userId);
        andHearingEventDefinitionsAreAvailable(hearingEventDefinitionsWithOnlySequencedEvents());
        andHearingHasBeenConfirmed(hearingConfirmed);
        andProgressionCaseDetailsAreAvailable(fromString(hearingConfirmed.getString(FIELD_CASE_ID)), hearingConfirmed.getString(FIELD_URN));

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

        InitiateHearingCommand initiateHearingCommand = UseCases.initiateHearing(requestSpec, builder-> {

            builder.getHearing().addDefendant(
                    Defendant.builder()
                            .withId(defendantId2)
                            .withPersonId(randomUUID())
                            .withFirstName(STRING.next())
                            .withLastName(STRING.next())
                            .withNationality(STRING.next())
                            .withGender(STRING.next())
                            .withAddress(
                                    Address.builder()
                                            .withAddress1(STRING.next())
                                            .withAddress2(STRING.next())
                                            .withAddress3(STRING.next())
                                            .withAddress4(STRING.next())
                                            .withPostCode(STRING.next())
                            )
                            .withDateOfBirth(PAST_LOCAL_DATE.next())
                            .withDefenceOrganisation(STRING.next())
                            .withInterpreter(
                                    Interpreter.builder()
                                            .withNeeded(false)
                                            .withLanguage(STRING.next())
                            )
                            .addDefendantCase(
                                    DefendantCase.builder()
                                            .withCaseId(builder.getCases().get(0).getCaseId())
                                            .withBailStatus(STRING.next())
                                            .withCustodyTimeLimitDate(FUTURE_ZONED_DATE_TIME.next())
                            )
                            .addOffence(
                                    Offence.builder()
                                            .withId(randomUUID())
                                            .withCaseId(builder.getCases().get(0).getCaseId())
                                            .withOffenceCode(STRING.next())
                                            .withWording(STRING.next())
                                            .withSection(STRING.next())
                                            .withStartDate(PAST_LOCAL_DATE.next())
                                            .withEndDate(PAST_LOCAL_DATE.next())
                                            .withOrderIndex(INTEGER.next())
                                            .withCount(INTEGER.next())
                                            .withConvictionDate(PAST_LOCAL_DATE.next())
                            )

            );
        });



        final JsonObject hearingConfirmed = hearingConfirmedFor(initiateHearingCommand.getHearing().getId(), initiateHearingCommand.getCases().get(0).getCaseId());

        givenAUserHasLoggedInAsACourtClerk(this.userId);
        andHearingEventDefinitionsAreAvailable(hearingEventDefinitionsWithOnlySequencedEvents());
        andHearingHasBeenConfirmed(hearingConfirmed);
        andProgressionCaseDetailsAreAvailable(initiateHearingCommand.getCases().get(0).getCaseId(), hearingConfirmed.getString(FIELD_URN));



        final List<DefenceCounselData> defenceCounsels = newArrayList(
                defenceCounsel(this.defenceCounselId, initiateHearingCommand.getHearing().getDefendants().get(0).getId()),
                defenceCounsel(this.defenceCounselId2, initiateHearingCommand.getHearing().getDefendants().get(1).getId()));

        whenHearingHasDefendantsWithDefenceCounsels(initiateHearingCommand.getHearing().getId(), defenceCounsels);



        thenHearingEventDefinitionsShouldProvideOptionToLogEventWithDefendantAndDefenceCouncil(initiateHearingCommand.getHearing().getId(), defenceCounsels);

        final DefenceCounselData randomDefenceCounsel = values(defenceCounsels).next();
        final HearingEvent mitigationEvent = mitigationEvent(this.hearingId, randomDefenceCounsel);
        whenUserLogsAnEvent(mitigationEvent);

        thenHearingEventIsRecorded(mitigationEvent);
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
    public void shouldIndicateIfALoggedHearingEventIsAlterable() {
        final JsonObject hearingConfirmed = hearingConfirmedFor(hearingId);

        givenAUserHasLoggedInAsACourtClerk(this.userId);
        andHearingEventDefinitionsAreAvailable(hearingEventDefinitionsWithPauseAndResumeEvents());
        andHearingHasBeenConfirmed(hearingConfirmed);
        andProgressionCaseDetailsAreAvailable(fromString(hearingConfirmed.getString(FIELD_CASE_ID)), hearingConfirmed.getString(FIELD_URN));

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
        final JsonObject hearingConfirmed = hearingConfirmedFor(hearingId);

        givenAUserHasLoggedInAsACourtClerk(this.userId);
        final HearingEventDefinitionData initialHearingEventDefinitions = hearingEventDefinitionsWithOnlySequencedEvents();
        andHearingEventDefinitionsAreAvailable(initialHearingEventDefinitions);
        andHearingHasBeenConfirmed(hearingConfirmed);
        andProgressionCaseDetailsAreAvailable(fromString(hearingConfirmed.getString(FIELD_CASE_ID)), hearingConfirmed.getString(FIELD_URN));

        whenHearingEventDefinitionsAreUpdated(hearingEventDefinitionsWithPauseAndResumeEvents());

        thenHearingEventDefinitionIsStillAvailable(values(initialHearingEventDefinitions.getEventDefinitions()).next());
    }

}
