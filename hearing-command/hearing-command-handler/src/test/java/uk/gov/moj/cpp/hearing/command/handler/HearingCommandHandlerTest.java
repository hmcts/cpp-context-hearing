package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromString;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingEventsLogAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CaseAssociated;
import uk.gov.moj.cpp.hearing.domain.event.CourtAssigned;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjournDateUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEnded;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeletionIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingStarted;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.RoomBooked;

import java.time.LocalDate;
import java.util.UUID;

import javax.json.JsonArrayBuilder;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingCommandHandlerTest {

    private static final String INITIATE_HEARING_COMMAND = "hearing.initiate-hearing";
    private static final String ALLOCATE_COURT_COMMAND = "hearing.allocate-court";
    private static final String BOOK_ROOM_COMMAND = "hearing.book-room";
    private static final String HEARING_START_COMMAND = "hearing.start";
    private static final String HEARING_END_COMMAND = "hearing.end";
    private static final String ADD_CASE_COMMAND = "hearing.add-case";
    private static final String ADD_PROSECUTION_COUNSEL_COMMAND = "hearing.add-prosecution-counsel";
    private static final String ADD_DEFENCE_COUNSEL_COMMAND = "hearing.add-defence-counsel";
    private static final String ADJOURN_DATE_COMMAND = "hearing.adjourn-date";
    private static final String LOG_HEARING_EVENT_COMMAND = "hearing.log-hearing-event";
    private static final String SAVE_DRAFT_RESULT_COMMAND = "hearing.save-draft-result";
    private static final String HEARING_CORRECT_EVENT_COMMAND = "hearing.correct-hearing-event";
    private static final String HEARING_EVENT_DEFINITIONS_COMMAND = "hearing.create-hearing-event-definitions";

    private static final String HEARING_INITIATED_EVENT = "hearing.hearing-initiated";
    private static final String CASE_ASSOCIATED_EVENT = "hearing.case-associated";
    private static final String COURT_ASSIGNED_EVENT = "hearing.court-assigned";
    private static final String ROOM_BOOKED_EVENT = "hearing.room-booked";
    private static final String HEARING_STARTED_EVENT = "hearing.started";
    private static final String HEARING_ENDED_EVENT = "hearing.ended";
    private static final String PROSECUTION_COUNSEL_ADDED_EVENT = "hearing.prosecution-counsel-added";
    private static final String DEFENCE_COUNSEL_ADDED_EVENT = "hearing.defence-counsel-added";
    private static final String ADJOURN_DATE_UPDATED_EVENT = "hearing.adjourn-date-updated";
    private static final String HEARING_EVENT_ADJOURN_DATE_UPDATED = "hearing.adjourn-date-updated";
    private static final String HEARING_EVENT_LOGGED_EVENT = "hearing.hearing-event-logged";
    private static final String HEARING_EVENT_DELETED_EVENT = "hearing.hearing-event-deleted";
    private static final String HEARING_DRAFT_RESULT_SAVED_EVENT = "hearing.draft-result-saved";
    private static final String HEARING_EVENT_DEFINITIONS_CREATED_EVENT = "hearing.hearing-event-definitions-created";
    private static final String HEARING_EVENT_DELETION_IGNORED_EVENT = "hearing.hearing-event-deletion-ignored";
    private static final String HEARING_EVENT_IGNORED_EVENT = "hearing.hearing-event-ignored";

    private static final String FIELD_ID = "id";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_LATEST_HEARING_EVENT_ID = "latestHearingEventId";
    private static final String FIELD_TIMESTAMP = "timestamp";

    private static final String FIELD_ACTION_LABEL = "actionLabel";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_SEQUENCE = "sequence";
    private static final String FIELD_CASE_ATTRIBUTE = "caseAttribute";
    private static final String FIELD_EVENT_DEFINITIONS = "eventDefinitions";

    private static final String FIELD_REASON = "reason";

    private static final String FIELD_START_DATE_TIME = "startDateTime";
    private static final String FIELD_DURATION = "duration";
    private static final String FIELD_HEARING_TYPE = "hearingType";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_ROOM_NAME = "roomName";
    private static final String FIELD_COURT_CENTRE_NAME = "courtCentreName";
    private static final String FIELD_LOCAL_TIME = "localTime";
    private static final String FIELD_PERSON_ID = "personId";
    private static final String FIELD_ATTENDEE_ID = "attendeeId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_DEFENDANT_IDS = "defendantIds";
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_TARGET_ID = "targetId";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_DRAFT_RESULT = "draftResult";
    private static final String FIELD_START_DATE = "startDate";

    private static final UUID HEARING_ID = randomUUID();

    private static final UUID HEARING_EVENT_ID = randomUUID();
    private static final UUID LATEST_HEARING_EVENT_ID = randomUUID();
    private static final String TIMESTAMP = ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next());
    private static final String DIFFERENT_TIMESTAMP = ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next());

    private static final UUID DEFENDANT_ID = randomUUID();
    private static final UUID TARGET_ID = randomUUID();
    private static final UUID OFFENCE_ID = randomUUID();
    private static final String ARBITRARY_STRING_IMP_2_YRS = "imp 2 yrs";
    private static final UUID DEFENDANT_ID_2 = randomUUID();

    private static final String ACTION_LABEL = STRING.next();
    private static final String RECORDED_LABEL = STRING.next();
    private static final Integer SEQUENCE = INTEGER.next();
    private static final String ACTION_LABEL_2 = STRING.next();
    private static final String RECORDED_LABEL_2 = STRING.next();
    private static final Integer SEQUENCE_2 = INTEGER.next();
    private static final String CASE_ATTRIBUTE = STRING.next();

    private static final UUID PERSON_ID = randomUUID();
    private static final UUID ATTENDEE_ID = randomUUID();
    private static final String STATUS = STRING.next();
    private final LocalDate CURRENT_DATE = LocalDate.now();

    private static final UUID HEARING_EVENT_DEFINITIONS_ID = randomUUID();

    private static final String START_DATE_TIME = ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next());
    private static final Integer DURATION = INTEGER.next();
    private static final String HEARING_TYPE = STRING.next();
    private static final UUID CASE_ID = randomUUID();
    private static final String ROOM_NAME = STRING.next();
    private static final String COURT_CENTRE_NAME = STRING.next();
    private static final String END_DATE_TIME = ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next());
    private static final String START_DATE = PAST_LOCAL_DATE.next().toString();

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private Enveloper enveloper = createEnveloperWithEvents(
            HearingEventLogged.class, DraftResultSaved.class, HearingEventDefinitionsCreated.class,
            HearingEventDeletionIgnored.class, HearingEventDeleted.class, HearingEventIgnored.class,
            HearingInitiated.class, CaseAssociated.class, CourtAssigned.class,
            RoomBooked.class, HearingStarted.class, HearingEnded.class, ProsecutionCounselAdded.class,
            DefenceCounselAdded.class, HearingAdjournDateUpdated.class);

    @InjectMocks
    private HearingCommandHandler hearingCommandHandler;

    @Before
    public void setup() {
        when(eventSource.getStreamById(HEARING_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, HearingAggregate.class)).thenReturn(new HearingAggregate());
        when(aggregateService.get(eventStream, HearingEventsLogAggregate.class)).thenReturn(new HearingEventsLogAggregate());
    }

    @Test
    public void shouldRaiseHearingInitiatedEventWhenOnlyRequiredFieldsAreAvailable() throws Exception {
        final JsonEnvelope command = createInitiateHearingCommandWithOnlyRequiredFields();

        hearingCommandHandler.initiateHearing(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_INITIATED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.startDateTime", representsSameTime(START_DATE_TIME)),
                                withJsonPath("$.duration", equalTo(DURATION)),
                                withJsonPath("$.hearingType", equalTo(HEARING_TYPE))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseMultipleEventsWhenAllTheFieldsAreAvailableForInitiateHearing() throws Exception {
        final JsonEnvelope command = createInitiateHearingCommand();

        hearingCommandHandler.initiateHearing(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_INITIATED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.startDateTime", representsSameTime(START_DATE_TIME)),
                                withJsonPath("$.duration", equalTo(DURATION)),
                                withJsonPath("$.hearingType", equalTo(HEARING_TYPE))
                        ))).thatMatchesSchema(),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(CASE_ASSOCIATED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.caseId", equalTo(CASE_ID.toString()))
                        ))).thatMatchesSchema(),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(COURT_ASSIGNED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.courtCentreName", equalTo(COURT_CENTRE_NAME))
                        ))).thatMatchesSchema(),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(ROOM_BOOKED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.roomName", equalTo(ROOM_NAME))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseCourtAssignedEvent() throws Exception {
        final JsonEnvelope command = createAllocateCourtCommand();

        hearingCommandHandler.allocateCourt(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(COURT_ASSIGNED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.courtCentreName", equalTo(COURT_CENTRE_NAME))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseRoomBookedEvent() throws Exception {
        final JsonEnvelope command = createBookRoomCommand();

        hearingCommandHandler.bookRoom(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(ROOM_BOOKED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.roomName", equalTo(ROOM_NAME))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseHearingStartedEvent() throws Exception {
        final JsonEnvelope command = createStartHearingCommand();

        hearingCommandHandler.start(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_STARTED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.startTime", representsSameTime(START_DATE_TIME))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseHearingEndedEvent() throws Exception {
        final JsonEnvelope command = createEndHearingCommand();

        hearingCommandHandler.end(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_ENDED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.endTime", representsSameTime(END_DATE_TIME))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseHearingAdjournDateUpdatedEvent() throws Exception {
        final JsonEnvelope command = createAdjournHearingDateCommand();

        hearingCommandHandler.adjournHearingDate(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(ADJOURN_DATE_UPDATED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.startDate", representsSameTime(START_DATE))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseCaseAssociatedEvent() throws Exception {
        final JsonEnvelope command = createAddCaseCommand();

        hearingCommandHandler.addCase(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(CASE_ASSOCIATED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.caseId", equalTo(CASE_ID.toString()))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseProsecutionCounselAddedEvent() throws Exception {
        final JsonEnvelope command = createAddProsecutionCounselCommand();

        hearingCommandHandler.addProsecutionCounsel(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(PROSECUTION_COUNSEL_ADDED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.personId", equalTo(PERSON_ID.toString())),
                                withJsonPath("$.attendeeId", equalTo(ATTENDEE_ID.toString())),
                                withJsonPath("$.status", equalTo(STATUS)),
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString()))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseDefenceCounselAddedEvent() throws Exception {
        final JsonEnvelope command = createAddDefenceCounselCommand();

        hearingCommandHandler.addDefenceCounsel(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(DEFENCE_COUNSEL_ADDED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.personId", equalTo(PERSON_ID.toString())),
                                withJsonPath("$.attendeeId", equalTo(ATTENDEE_ID.toString())),
                                withJsonPath("$.status", equalTo(STATUS)),
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.defendantIds", hasSize(2)),
                                withJsonPath("$.defendantIds", hasItems(DEFENDANT_ID.toString(), DEFENDANT_ID_2.toString()))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseHearingEventDefinitionsCreated() throws Exception {
        when(eventSource.getStreamById(HEARING_EVENT_DEFINITIONS_ID)).thenReturn(eventStream);
        final JsonEnvelope command = createHearingEventDefinitions();

        hearingCommandHandler.createHearingEventDefinitions(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_DEFINITIONS_CREATED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_ID), equalTo(HEARING_EVENT_DEFINITIONS_ID.toString())),
                                withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), equalTo(ACTION_LABEL)),
                                withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                                withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE), equalTo(SEQUENCE)),

                                withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), equalTo(ACTION_LABEL_2)),
                                withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL_2)),
                                withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE), equalTo(SEQUENCE_2)),
                                withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTE), equalTo(CASE_ATTRIBUTE))
                        ))
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseHearingEventLoggedIfNotAlreadyLogged() throws Exception {
        final JsonEnvelope command = createLogHearingEventCommand();

        hearingCommandHandler.logHearingEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_LOGGED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                                withJsonPath(format("$.%s", FIELD_TIMESTAMP), representsSameTime(TIMESTAMP))
                        ))
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldIgnoreLogHearingEventIfItsAlreadyBeenLogged() throws Exception {
        final HearingEventsLogAggregate hearingEventsLogAggregate = new HearingEventsLogAggregate();
        hearingEventsLogAggregate.apply(new HearingEventLogged(HEARING_EVENT_ID, HEARING_ID, RECORDED_LABEL, fromString(TIMESTAMP)));
        when(aggregateService.get(eventStream, HearingEventsLogAggregate.class)).thenReturn(hearingEventsLogAggregate);

        final JsonEnvelope command = createLogHearingEventCommand();

        hearingCommandHandler.logHearingEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_IGNORED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                                withJsonPath(format("$.%s", FIELD_TIMESTAMP), representsSameTime(TIMESTAMP)),
                                withJsonPath(format("$.%s", FIELD_REASON), equalTo("Already logged"))
                        ))
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldIgnoreLogHearingEventIfItsBeenDeleted() throws Exception {
        final HearingEventsLogAggregate hearingEventsLogAggregate = new HearingEventsLogAggregate();
        hearingEventsLogAggregate.apply(new HearingEventDeleted(HEARING_EVENT_ID));
        when(aggregateService.get(eventStream, HearingEventsLogAggregate.class)).thenReturn(hearingEventsLogAggregate);

        final JsonEnvelope command = createLogHearingEventCommand();

        hearingCommandHandler.logHearingEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_IGNORED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                                withJsonPath(format("$.%s", FIELD_TIMESTAMP), representsSameTime(TIMESTAMP)),
                                withJsonPath(format("$.%s", FIELD_REASON), equalTo("Already deleted"))
                        ))
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseLoggedAndDeletedHearingEventsWhenTimestampOfExistingHearingEventIsCorrected() throws Exception {
        final HearingEventsLogAggregate hearingEventsLogAggregate = new HearingEventsLogAggregate();
        hearingEventsLogAggregate.apply(new HearingEventLogged(HEARING_EVENT_ID, HEARING_ID, RECORDED_LABEL, fromString(TIMESTAMP)));
        when(aggregateService.get(eventStream, HearingEventsLogAggregate.class)).thenReturn(hearingEventsLogAggregate);

        final JsonEnvelope command = createCorrectHearingEventCommand();
        hearingCommandHandler.correctEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_LOGGED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(LATEST_HEARING_EVENT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                                withJsonPath(format("$.%s", FIELD_TIMESTAMP), representsSameTime(DIFFERENT_TIMESTAMP))
                        ))
                ).thatMatchesSchema(),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_DELETED_EVENT),
                        payloadIsJson(
                                withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID.toString()))
                        )
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldLogUpdatedEventAndIgnoreDeletionWhenTimestampIsCorrectedForHearingEventWhichHasNotBeenLogged() throws Exception {
        final JsonEnvelope command = createCorrectHearingEventCommand();
        hearingCommandHandler.correctEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_LOGGED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(LATEST_HEARING_EVENT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                                withJsonPath(format("$.%s", FIELD_TIMESTAMP), representsSameTime(DIFFERENT_TIMESTAMP))
                        ))
                ).thatMatchesSchema(),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_DELETION_IGNORED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_REASON), equalTo("Hearing Event not found"))
                        ))
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseDraftResultSaved() throws Exception {
        final JsonEnvelope command = createSaveDraftResultCommand();

        hearingCommandHandler.saveDraftResult(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_DRAFT_RESULT_SAVED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_DEFENDANT_ID), equalTo(DEFENDANT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_OFFENCE_ID), equalTo(OFFENCE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_DRAFT_RESULT), equalTo(ARBITRARY_STRING_IMP_2_YRS)),
                                withJsonPath(format("$.%s", FIELD_TARGET_ID), equalTo(TARGET_ID.toString()))
                        ))
                ).thatMatchesSchema()
        ));
    }

//   TODO jayen needs to verify it after the merge
//   @Test
//    public void shouldAdjournHearingDate() throws Exception {
//        final JsonEnvelope command = createAdjournHearingDateCommand();
//
//        when(eventSource.getStreamById(hearingId)).thenReturn(eventStream);
//        when(aggregateService.get(eventStream, HearingAggregate.class)).thenReturn(hearingAggregate);
//        when(hearingAggregate.adjournHearingDate(hearingId, CURRENT_DATE)).thenReturn(Stream.of(
//                new HearingAdjournDateUpdated(hearingId, CURRENT_DATE)));
//        when(enveloper.withMetadataFrom(command)).thenReturn(
//                createEnveloperWithEvents(HearingAdjournDateUpdated.class).withMetadataFrom(command));
//
//        hearingCommandHandler.adjournHearingDate(command);
//
//        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
//                jsonEnvelope(
//                        withMetadataEnvelopedFrom(command)
//                                .withName(HEARING_EVENT_ADJOURN_DATE_UPDATED),
//                        payloadIsJson(allOf(
//                                withJsonPath("$.hearingId", equalTo(hearingId.toString())),
//                                withJsonPath("$.startDate", equalTo(CURRENT_DATE.toString()))
//                                )
//                        )
//                )
//        ));
//    }

    private static JsonEnvelope createHearingEventDefinitions() {
        final JsonArrayBuilder eventDefinitionsBuilder = createArrayBuilder()
                .add(createObjectBuilder()
                        .add(FIELD_ACTION_LABEL, ACTION_LABEL)
                        .add(FIELD_RECORDED_LABEL, RECORDED_LABEL)
                        .add(FIELD_SEQUENCE, SEQUENCE))
                .add(createObjectBuilder()
                        .add(FIELD_ACTION_LABEL, ACTION_LABEL_2)
                        .add(FIELD_RECORDED_LABEL, RECORDED_LABEL_2)
                        .add(FIELD_SEQUENCE, SEQUENCE_2)
                        .add(FIELD_CASE_ATTRIBUTE, CASE_ATTRIBUTE));

        return envelopeFrom(metadataWithRandomUUID(HEARING_EVENT_DEFINITIONS_COMMAND),
                createObjectBuilder()
                        .add(FIELD_ID, HEARING_EVENT_DEFINITIONS_ID.toString())
                        .add(FIELD_EVENT_DEFINITIONS, eventDefinitionsBuilder)
                        .build());
    }

    private JsonEnvelope createInitiateHearingCommandWithOnlyRequiredFields() {
        return envelope()
                .with(metadataWithRandomUUID(INITIATE_HEARING_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(START_DATE_TIME, FIELD_START_DATE_TIME)
                .withPayloadOf(DURATION, FIELD_DURATION)
                .withPayloadOf(HEARING_TYPE, FIELD_HEARING_TYPE)
                .build();
    }

    private JsonEnvelope createInitiateHearingCommand() {
        return envelope()
                .with(metadataWithRandomUUID(INITIATE_HEARING_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(START_DATE_TIME, FIELD_START_DATE_TIME)
                .withPayloadOf(DURATION, FIELD_DURATION)
                .withPayloadOf(HEARING_TYPE, FIELD_HEARING_TYPE)
                .withPayloadOf(CASE_ID, FIELD_CASE_ID)
                .withPayloadOf(COURT_CENTRE_NAME, FIELD_COURT_CENTRE_NAME)
                .withPayloadOf(ROOM_NAME, FIELD_ROOM_NAME)
                .build();
    }

    private JsonEnvelope createAllocateCourtCommand() {
        return envelope()
                .with(metadataWithRandomUUID(ALLOCATE_COURT_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(COURT_CENTRE_NAME, FIELD_COURT_CENTRE_NAME)
                .build();
    }

    private JsonEnvelope createBookRoomCommand() {
        return envelope()
                .with(metadataWithRandomUUID(BOOK_ROOM_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(ROOM_NAME, FIELD_ROOM_NAME)
                .build();
    }

    private JsonEnvelope createStartHearingCommand() {
        return envelope()
                .with(metadataWithRandomUUID(HEARING_START_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(START_DATE_TIME, FIELD_LOCAL_TIME)
                .build();
    }

    private JsonEnvelope createEndHearingCommand() {
        return envelope()
                .with(metadataWithRandomUUID(HEARING_END_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(END_DATE_TIME, FIELD_LOCAL_TIME)
                .build();
    }

    private JsonEnvelope createAdjournHearingDateCommand() {
        return envelope()
                .with(metadataWithRandomUUID(ADJOURN_DATE_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(START_DATE, FIELD_START_DATE)
                .build();
    }

    private JsonEnvelope createAddCaseCommand() {
        return envelope()
                .with(metadataWithRandomUUID(ADD_CASE_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(CASE_ID, FIELD_CASE_ID)
                .build();
    }

    private JsonEnvelope createLogHearingEventCommand() {
        return envelope()
                .with(metadataWithRandomUUID(LOG_HEARING_EVENT_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(HEARING_EVENT_ID, FIELD_HEARING_EVENT_ID)
                .withPayloadOf(RECORDED_LABEL, FIELD_RECORDED_LABEL)
                .withPayloadOf(TIMESTAMP, FIELD_TIMESTAMP)
                .build();
    }

    private JsonEnvelope createCorrectHearingEventCommand() {
        return envelope()
                .with(metadataWithRandomUUID(HEARING_CORRECT_EVENT_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(HEARING_EVENT_ID, FIELD_HEARING_EVENT_ID)
                .withPayloadOf(RECORDED_LABEL, FIELD_RECORDED_LABEL)
                .withPayloadOf(LATEST_HEARING_EVENT_ID, FIELD_LATEST_HEARING_EVENT_ID)
                .withPayloadOf(DIFFERENT_TIMESTAMP, FIELD_TIMESTAMP)
                .build();
    }

    private JsonEnvelope createSaveDraftResultCommand() {
        return envelope()
                .with(metadataWithRandomUUID(SAVE_DRAFT_RESULT_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(DEFENDANT_ID, FIELD_DEFENDANT_ID)
                .withPayloadOf(TARGET_ID, FIELD_TARGET_ID)
                .withPayloadOf(OFFENCE_ID, FIELD_OFFENCE_ID)
                .withPayloadOf(ARBITRARY_STRING_IMP_2_YRS, FIELD_DRAFT_RESULT)
                .build();
    }

    private JsonEnvelope createAddProsecutionCounselCommand() {
        return envelope()
                .with(metadataWithRandomUUID(ADD_PROSECUTION_COUNSEL_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(PERSON_ID, FIELD_PERSON_ID)
                .withPayloadOf(ATTENDEE_ID, FIELD_ATTENDEE_ID)
                .withPayloadOf(STATUS, FIELD_STATUS)
                .build();
    }

    private JsonEnvelope createAddDefenceCounselCommand() {
        JsonArrayBuilder defendantIdsBuilder = createArrayBuilder()
                .add(createObjectBuilder().add(FIELD_DEFENDANT_ID, DEFENDANT_ID.toString()))
                .add(createObjectBuilder().add(FIELD_DEFENDANT_ID, DEFENDANT_ID_2.toString()));

        return envelopeFrom(metadataWithRandomUUID(ADD_DEFENCE_COUNSEL_COMMAND),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, HEARING_ID.toString())
                        .add(FIELD_PERSON_ID, PERSON_ID.toString())
                        .add(FIELD_ATTENDEE_ID, ATTENDEE_ID.toString())
                        .add(FIELD_STATUS, STATUS)
                        .add(FIELD_DEFENDANT_IDS, defendantIdsBuilder)
                        .build());
    }

    private Matcher<String> representsSameTime(final String time) {
        //Framework JSON serialisation crops excess 0s from timestamp fields so we must compare against trimmed millisecond fields
        if (time.endsWith("000Z")) {
            return is(time.replace("000Z", "Z"));
        } else if (time.endsWith("00Z")) {
            return is(time.replace("00Z", "Z"));
        } else if (time.endsWith("0Z")) {
            return is(time.replace("0Z", "Z"));
        } else {
            return is(time);
        }
    }

}
