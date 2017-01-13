package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromJsonString;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonNumber;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonString;
import static uk.gov.justice.services.messaging.JsonObjects.getString;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.UUID;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.command.handler.converter.JsonToHearingConverter;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingEventsLogAggregate;
import uk.gov.moj.cpp.hearing.domain.command.InitiateHearing;
import uk.gov.moj.cpp.hearing.domain.event.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventCorrected;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingCommandHandlerTest {

    private static final String LIST_HEARING_COMMAND = "hearing.initiate-hearing";
    private static final String LOG_HEARING_EVENT_COMMAND = "hearing.log-hearing-event";
    private static final String SAVE_DRAFT_RESULT_COMMAND = "hearing.save-draft-result";
    private static final String HEARING_CORRECT_EVENT_COMMAND = "hearing.correct-hearing-event";
    private static final String HEARING_EVENT_DEFINITIONS_COMMAND = "hearing.create-hearing-event-definitions";

    private static final String HEARING_INITIATED_EVENT = "hearing.hearing-initiated";
    private static final String PROSECUTION_COUNSEL_ADDED_EVENT = "hearing.prosecution-counsel-added";
    private static final String HEARING_EVENT_LOGGED_EVENT = "hearing.hearing-event-logged";
    private static final String HEARING_EVENT_CORRECTED_EVENT = "hearing.hearing-event-corrected";
    private static final String HEARING_DRAFT_RESULT_SAVED_EVENT = "hearing.draft-result-saved";
    private static final String HEARING_EVENT_DEFINITIONS_CREATED_EVENT = "hearing.hearing-event-definitions-created";

    private static final String FIELD_ID = "id";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_TIMESTAMP = "timestamp";

    private static final String FIELD_ACTION_LABEL = "actionLabel";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_SEQUENCE = "sequence";
    private static final String FIELD_CASE_ATTRIBUTE = "caseAttribute";
    private static final String FIELD_EVENT_DEFINITIONS = "eventDefinitions";

    private static final UUID HEARING_ID = randomUUID();

    private static final UUID HEARING_EVENT_ID = randomUUID();
    private static final String TIMESTAMP = ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next());
    private static final String DIFFERENT_TIMESTAMP = ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next());

    private static final String DEFENDANT_ID = "defendantId";
    private static final UUID DEFENDANT_ID_VALUE = randomUUID();
    private static final String TARGET_ID = "targetId";
    private static final UUID TARGET_ID_VALUE = randomUUID();
    private static final String OFFENCE_ID = "offenceId";
    private static final UUID OFFENCE_ID_VALUE = randomUUID();
    private static final String DRAFT_RESULT = "draftResult";
    private static final String ARBITRARY_STRING_IMP_2_YRS = "imp 2 yrs";

    private static final String ACTION_LABEL = STRING.next();
    private static final String RECORDED_LABEL = STRING.next();
    private static final Integer SEQUENCE = INTEGER.next();
    private static final String ACTION_LABEL_2 = STRING.next();
    private static final String RECORDED_LABEL_2 = STRING.next();
    private static final Integer SEQUENCE_2 = INTEGER.next();
    private static final String CASE_ATTRIBUTE = STRING.next();

    private final UUID personId = randomUUID();
    private final UUID hearingId = randomUUID();
    private final UUID attendeeId = randomUUID();
    private final String status = STRING.next();

    private static final UUID HEARING_EVENT_DEFINITIONS_ID = randomUUID();

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private JsonToHearingConverter jsonToHearingConverter;

    @Mock
    private AggregateService aggregateService;

    @Mock
    private HearingAggregate hearingAggregate;

    private HearingEventsLogAggregate hearingEventAggregate;

    @Spy
    private Enveloper enveloper = createEnveloperWithEvents(
            HearingEventLogged.class,
            HearingEventCorrected.class,
            DraftResultSaved.class,
            HearingEventDefinitionsCreated.class);

    @InjectMocks
    private HearingCommandHandler hearingCommandHandler;

    @Before
    public void setup() {
        when(eventSource.getStreamById(HEARING_ID)).thenReturn(eventStream);

        when(eventSource.getStreamById(HEARING_EVENT_ID)).thenReturn(eventStream);
    }

    @Test
    public void shouldHandlerListHearingCommand() throws Exception {
        final InitiateHearing initiateHearing = createHearing(HEARING_ID);
        final JsonEnvelope listHearingCommand = createListHearingCommand(initiateHearing);

        when(jsonToHearingConverter.convertToInitiateHearing(listHearingCommand)).thenReturn(initiateHearing);
        when(eventSource.getStreamById(HEARING_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, HearingAggregate.class)).thenReturn(hearingAggregate);
        when(hearingAggregate.initiateHearing(initiateHearing)).thenReturn(Stream.of(createHearingListed(initiateHearing)));
        when(enveloper.withMetadataFrom(listHearingCommand)).thenReturn(
                createEnveloperWithEvents(HearingInitiated.class).withMetadataFrom(listHearingCommand));

        hearingCommandHandler.initiateHearing(listHearingCommand);

        final JsonEnvelope resultEvent = verifyAppendAndGetArgumentFrom(eventStream).findFirst().get();
        final Metadata resultMetadata = resultEvent.metadata();
        final JsonObject resultPayload = resultEvent.payloadAsJsonObject();
        // and
        assertThat(resultMetadata.name(), is(HEARING_INITIATED_EVENT));
        // and
        assertThat(resultPayload, isFrom(initiateHearing));

    }

    @Test
    public void shouldAddProsecutionCounsel() throws Exception {
        final JsonEnvelope command = createAddProsecutionCounselCommand();

        when(eventSource.getStreamById(hearingId)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, HearingAggregate.class)).thenReturn(hearingAggregate);
        when(hearingAggregate.addProsecutionCounsel(hearingId, attendeeId, personId, status)).thenReturn(Stream.of(
                new ProsecutionCounselAdded(hearingId, attendeeId, personId, status)));
        when(enveloper.withMetadataFrom(command)).thenReturn(
                createEnveloperWithEvents(ProsecutionCounselAdded.class).withMetadataFrom(command));

        hearingCommandHandler.addProsecutionCounsel(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(PROSECUTION_COUNSEL_ADDED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.personId", equalTo(personId.toString())),
                                withJsonPath("$.attendeeId", equalTo(attendeeId.toString())),
                                withJsonPath("$.status", equalTo(status)),
                                withJsonPath("$.hearingId", equalTo(hearingId.toString()))
                                )
                        )
                )
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
    public void shouldRaiseHearingEventLogged() throws Exception {
        when(aggregateService.get(eventStream, HearingEventsLogAggregate.class)).thenReturn(new HearingEventsLogAggregate());

        final JsonEnvelope command = createHearingEventLoggedCommand();

        hearingCommandHandler.logHearingEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_LOGGED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_ID), equalTo(HEARING_EVENT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                                withJsonPath(format("$.%s", FIELD_TIMESTAMP), representsSameTime(TIMESTAMP))
                        ))
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseEventCorrected() throws Exception {
        hearingEventAggregate = new HearingEventsLogAggregate();
        hearingEventAggregate.apply(new HearingEventLogged(HEARING_EVENT_ID, HEARING_ID, RECORDED_LABEL, ZonedDateTime.parse(TIMESTAMP)));

        when(aggregateService.get(eventStream, HearingEventsLogAggregate.class)).thenReturn(hearingEventAggregate);

        final JsonEnvelope command = createEventCorrectedCommand();

        hearingCommandHandler.correctEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_CORRECTED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_TIMESTAMP), representsSameTime(DIFFERENT_TIMESTAMP))
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
                                withJsonPath(format("$.%s", DEFENDANT_ID), equalTo(DEFENDANT_ID_VALUE.toString())),
                                withJsonPath(format("$.%s", OFFENCE_ID), equalTo(OFFENCE_ID_VALUE.toString())),
                                withJsonPath(format("$.%s", DRAFT_RESULT), equalTo(ARBITRARY_STRING_IMP_2_YRS)),
                                withJsonPath(format("$.%s", TARGET_ID), equalTo(TARGET_ID_VALUE.toString()))
                        ))
                ).thatMatchesSchema()
        ));
    }

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

    private JsonEnvelope createListHearingCommand(InitiateHearing initiateHearing) {
        final JsonObject metadataAsJsonObject =
                createObjectBuilder()
                        .add(ID, UUID.next().toString())
                        .add(NAME, LIST_HEARING_COMMAND)
                        .build();

        final JsonObject payloadAsJsonObject = createObjectBuilder()
                .add("hearingId", initiateHearing.getHearingId().toString())
                .add("startDateTime", ZonedDateTimes.toString(initiateHearing.getStartDateTime()))
                .add("duration", initiateHearing.getDuration())
                .build();

        return DefaultJsonEnvelope.envelopeFrom(JsonObjectMetadata.metadataFrom(metadataAsJsonObject), payloadAsJsonObject);

    }

    private HearingInitiated createHearingListed(InitiateHearing initiateHearing) {
        return new HearingInitiated(initiateHearing.getHearingId(),
                initiateHearing.getStartDateTime(), initiateHearing.getDuration(), initiateHearing.getHearingType());
    }

    private InitiateHearing createHearing(UUID hearingId) {
        final ZonedDateTime startDateOfHearing = new UtcClock().now();
        final Integer duration = INTEGER.next();
        final String random = "TRAIL";
        return new InitiateHearing(hearingId, startDateOfHearing, duration, random);
    }

    private JsonEnvelope createHearingEventLoggedCommand() {
        return envelope()
                .with(metadataWithRandomUUID(LOG_HEARING_EVENT_COMMAND))
                .withPayloadOf(HEARING_EVENT_ID, FIELD_ID)
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(RECORDED_LABEL, FIELD_RECORDED_LABEL)
                .withPayloadOf(TIMESTAMP, FIELD_TIMESTAMP)
                .build();
    }

    private JsonEnvelope createEventCorrectedCommand() {
        return envelope()
                .with(metadataWithRandomUUID(HEARING_CORRECT_EVENT_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(HEARING_EVENT_ID, FIELD_HEARING_EVENT_ID)
                .withPayloadOf(DIFFERENT_TIMESTAMP, FIELD_TIMESTAMP)
                .build();
    }

    private JsonEnvelope createSaveDraftResultCommand() {
        return envelope()
                .with(metadataWithRandomUUID(SAVE_DRAFT_RESULT_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(DEFENDANT_ID_VALUE, DEFENDANT_ID)
                .withPayloadOf(TARGET_ID_VALUE, TARGET_ID)
                .withPayloadOf(OFFENCE_ID_VALUE, OFFENCE_ID)
                .withPayloadOf(ARBITRARY_STRING_IMP_2_YRS, DRAFT_RESULT)
                .build();
    }

    private JsonEnvelope createAddProsecutionCounselCommand() {
        return envelope()
                .with(metadataWithRandomUUID(PROSECUTION_COUNSEL_ADDED_EVENT))
                .withPayloadOf(hearingId, "hearingId")
                .withPayloadOf(personId, "personId")
                .withPayloadOf(attendeeId, "attendeeId")
                .withPayloadOf(status, "status")
                .build();
    }

    private Matcher<JsonObject> isFrom(final InitiateHearing initiateHearing) {
        return new TypeSafeDiagnosingMatcher<JsonObject>() {

            @Override
            public void describeTo(Description description) {
                description.appendText(initiateHearing.toString());
            }

            @Override
            protected boolean matchesSafely(JsonObject resultPayload, Description description) {
                boolean returnStatus = true;

                if (!Objects.equals(initiateHearing.getHearingId(), getUUID(resultPayload, "hearingId").get())) {
                    description.appendText(format("HearingId Mismatch:initiateHearing:%s, hearingListed%s",
                            initiateHearing.getHearingId(), getUUID(resultPayload, "hearingId").get()));
                    returnStatus = false;
                }

                if (!Objects.equals(initiateHearing.getDuration(), getJsonNumber(resultPayload, "duration").get().intValue())) {
                    description.appendText(format("Duration Mismatch:initiateHearing:%s, hearingListed%s",
                            initiateHearing.getDuration(), getJsonNumber(resultPayload, "duration").get().intValue()));
                    returnStatus = false;
                }
                if (!Objects.equals(initiateHearing.getHearingType(), getJsonString(resultPayload, "hearingType").get().getString())) {
                    description.appendText(format("HearingType Mismatch:initiateHearing:%s, hearingType %s",
                            initiateHearing.getHearingType(), getJsonString(resultPayload, "hearingType").get().getString()));
                    returnStatus = false;
                }

                if (!Objects.equals(initiateHearing.getStartDateTime().toLocalDate(), fromJsonString(resultPayload.getJsonString("startDateTime")).toLocalDate())) {
                    description.appendText(format("StartDateOfHearing Mismatch:initiateHearing:%s, hearingListed%s",
                            initiateHearing.getStartDateTime(), getString(resultPayload, "startDateTime").get()));
                    returnStatus = false;
                }

                return returnStatus;
            }
        };
    }

    private Matcher<String> representsSameTime(String time) {
        //Framework JSON serialisation crops excess 0s from timestamp fields so we must compare against trimmed millisecond fields
        return anyOf(is(time), is(time.replace("0Z", "Z")), is(time.replace("00Z", "Z")), is(time.replace("000Z", "Z")));
    }

}
