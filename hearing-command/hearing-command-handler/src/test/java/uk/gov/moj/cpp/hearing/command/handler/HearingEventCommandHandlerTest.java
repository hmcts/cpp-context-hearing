package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingEventDefinitionAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonArrayBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.domain.event.Initiated;

@RunWith(MockitoJUnitRunner.class)
public class HearingEventCommandHandlerTest {

    private static final String HEARING_EVENT_DEFINITIONS_CREATED_EVENT = "hearing.hearing-event-definitions-created";
    private static final String HEARING_EVENT_DEFINITIONS_DELETED_EVENT = "hearing.hearing-event-definitions-deleted";

    private static final String FIELD_GENERIC_ID = "id";

    private static final String FIELD_ACTION_LABEL = "actionLabel";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_SEQUENCE = "sequence";
    private static final String FIELD_SEQUENCE_TYPE = "sequenceType";
    private static final String FIELD_CASE_ATTRIBUTE = "caseAttribute";
    private static final String FIELD_ALTERABLE = "alterable";
    private static final String FIELD_EVENT_DEFINITIONS = "eventDefinitions";
    private static final String FIELD_GROUP_LABEL = "groupLabel";
    private static final String FIELD_ACTION_LABEL_EXTENSION = "actionLabelExtension";

    private static final UUID HEARING_ID = randomUUID();

    private static final String ACTION_LABEL = STRING.next();
    private static final String RECORDED_LABEL = STRING.next();
    private static final Integer SEQUENCE = INTEGER.next();
    private static final String ACTION_LABEL_2 = STRING.next();
    private static final String RECORDED_LABEL_2 = STRING.next();
    private static final Integer SEQUENCE_2 = INTEGER.next();
    private static final String CASE_ATTRIBUTE = STRING.next();
    private static final String ACTION_LABEL_3 = STRING.next();
    private static final String RECORDED_LABEL_3 = STRING.next();
    private static final String SEQUENCE_TYPE = STRING.next();
    private static final String GROUP_LABEL = STRING.next();
    private static final String ACTION_LABEL_EXTENSION = STRING.next();

    private static final UUID HEARING_EVENT_DEFINITIONS_ID = randomUUID();
    private static final UUID HEARING_EVENT_DEFINITION_ID = randomUUID();
    private static final UUID HEARING_EVENT_DEFINITION_ID_2 = randomUUID();
    private static final UUID HEARING_EVENT_DEFINITION_ID_3 = randomUUID();

    private static final boolean ALTERABLE = BOOLEAN.next();
    private static final boolean ALTERABLE_2 = BOOLEAN.next();
    private static final boolean ALTERABLE_3 = BOOLEAN.next();


    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;
    @Spy
    private Enveloper enveloper = createEnveloperWithEvents(
            HearingEventLogged.class, HearingEventDefinitionsCreated.class,
            HearingEventDeleted.class, HearingEventIgnored.class,
            HearingEventDefinitionsDeleted.class);

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @InjectMocks
    private HearingEventCommandHandler hearingEventCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldAlwaysRaiseHearingEventDefinitionsDeletedAndCreatedEvents() throws Exception {

        when(eventSource.getStreamById(HEARING_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, NewModelHearingAggregate.class)).thenReturn(new NewModelHearingAggregate());

        when(eventSource.getStreamById(HEARING_EVENT_DEFINITIONS_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, HearingEventDefinitionAggregate.class)).thenReturn(new HearingEventDefinitionAggregate());

        final JsonArrayBuilder eventDefinitionsBuilder = createArrayBuilder()
                .add(createObjectBuilder()
                        .add(FIELD_GENERIC_ID, HEARING_EVENT_DEFINITION_ID.toString())
                        .add(FIELD_ACTION_LABEL, ACTION_LABEL)
                        .add(FIELD_RECORDED_LABEL, RECORDED_LABEL)
                        .add(FIELD_SEQUENCE, SEQUENCE)
                        .add(FIELD_SEQUENCE_TYPE, SEQUENCE_TYPE)
                        .add(FIELD_ALTERABLE, ALTERABLE)
                )
                .add(createObjectBuilder()
                        .add(FIELD_GENERIC_ID, HEARING_EVENT_DEFINITION_ID_2.toString())
                        .add(FIELD_GROUP_LABEL, GROUP_LABEL)
                        .add(FIELD_ACTION_LABEL, ACTION_LABEL_2)
                        .add(FIELD_ACTION_LABEL_EXTENSION, ACTION_LABEL_EXTENSION)
                        .add(FIELD_RECORDED_LABEL, RECORDED_LABEL_2)
                        .add(FIELD_SEQUENCE, SEQUENCE_2)
                        .add(FIELD_SEQUENCE_TYPE, SEQUENCE_TYPE)
                        .add(FIELD_CASE_ATTRIBUTE, CASE_ATTRIBUTE)
                        .add(FIELD_ALTERABLE, ALTERABLE_2)
                )
                .add(createObjectBuilder()
                        .add(FIELD_GENERIC_ID, HEARING_EVENT_DEFINITION_ID_3.toString())
                        .add(FIELD_ACTION_LABEL, ACTION_LABEL_3)
                        .add(FIELD_RECORDED_LABEL, RECORDED_LABEL_3)
                        .add(FIELD_ALTERABLE, ALTERABLE_3)
                );

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUIDAndName(),
                createObjectBuilder()
                        .add(FIELD_GENERIC_ID, HEARING_EVENT_DEFINITIONS_ID.toString())
                        .add(FIELD_EVENT_DEFINITIONS, eventDefinitionsBuilder)
                        .build());

        hearingEventCommandHandler.createHearingEventDefinitions(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_DEFINITIONS_DELETED_EVENT),
                        payloadIsJson(
                                withJsonPath(format("$.%s", FIELD_GENERIC_ID), equalTo(HEARING_EVENT_DEFINITIONS_ID.toString()))
                        )).thatMatchesSchema(),

                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_DEFINITIONS_CREATED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_GENERIC_ID), equalTo(HEARING_EVENT_DEFINITIONS_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_EVENT_DEFINITIONS), hasSize(3)),

                                withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), equalTo(HEARING_EVENT_DEFINITION_ID.toString())),
                                withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), equalTo(ACTION_LABEL)),
                                withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                                withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), equalTo(ALTERABLE)),
                                withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE), equalTo(SEQUENCE)),
                                withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE_TYPE), equalTo(SEQUENCE_TYPE)),
                                withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTE)),
                                withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                                withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION)),

                                withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), equalTo(HEARING_EVENT_DEFINITION_ID_2.toString())),
                                withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), equalTo(ACTION_LABEL_2)),
                                withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL_2)),
                                withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), equalTo(ALTERABLE_2)),
                                withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE), equalTo(SEQUENCE_2)),
                                withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE_TYPE), equalTo(SEQUENCE_TYPE)),
                                withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTE), equalTo(CASE_ATTRIBUTE)),
                                withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL), equalTo(GROUP_LABEL)),
                                withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION), equalTo(ACTION_LABEL_EXTENSION)),

                                withJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), equalTo(HEARING_EVENT_DEFINITION_ID_3.toString())),
                                withJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), equalTo(ACTION_LABEL_3)),
                                withJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL_3)),
                                withJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), equalTo(ALTERABLE_3)),
                                withoutJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE)),
                                withoutJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE_TYPE)),
                                withoutJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTE)),
                                withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                                withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION))
                        ))).thatMatchesSchema()
        ));
    }


    @Test
    public void logHearingEvent_shouldRaiseHearingEventLogged() throws Exception {

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        LogEventCommand logEvent = new LogEventCommand(randomUUID(), randomUUID(), randomUUID(), STRING.next(),
                PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), false,null);

        setupMockedEventStream(logEvent.getHearingId(), this.eventStream, with(new NewModelHearingAggregate(), a -> {
            a.apply(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));
        }));

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.log-hearing-event"),
                objectToJsonObjectConverter.convert(logEvent));

        hearingEventCommandHandler.logHearingEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.hearing-event-logged"),
                        payloadIsJson(allOf(
                                withJsonPath("$.alterable", is(logEvent.getAlterable())),
                                withJsonPath("$.hearingEventDefinitionId", is(logEvent.getHearingEventDefinitionId().toString())),
                                withJsonPath("$.hearingEventId", is(logEvent.getHearingEventId().toString())),
                                withJsonPath("$.hearingId", is(logEvent.getHearingId().toString())),
                                withJsonPath("$.recordedLabel", is(logEvent.getRecordedLabel())),
                                withJsonPath("$.eventTime", is(logEvent.getEventTime().toLocalDateTime().atZone(ZoneId.of("Z")).toString())),
                                withoutJsonPath("$.lastHearingEventId"),
                                withJsonPath("$.lastModifiedTime", is(logEvent.getLastModifiedTime().toLocalDateTime().atZone(ZoneId.of("Z")).toString())),
                                withJsonPath("$.caseId", is(initiateHearingCommand.getCases().get(0).getCaseId().toString())),
                                withJsonPath("$.caseUrn", is(initiateHearingCommand.getCases().get(0).getUrn())),
                                withJsonPath("$.courtCentreId", is(initiateHearingCommand.getHearing().getCourtCentreId().toString())),
                                withJsonPath("$.courtCentreName", is(initiateHearingCommand.getHearing().getCourtCentreName())),
                                withJsonPath("$.courtRoomId", is(initiateHearingCommand.getHearing().getCourtRoomId().toString())),
                                withJsonPath("$.courtRoomName", is(initiateHearingCommand.getHearing().getCourtRoomName())),
                                withJsonPath("$.hearingType", is(initiateHearingCommand.getHearing().getType()))
                        ))
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void logHearingEvent_shouldIgnoreLogEvent_givenEventHasAlreadyBeenLogged() throws Exception {

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        LogEventCommand logEvent = new LogEventCommand(randomUUID(), initiateHearingCommand.getHearing().getId(),
                randomUUID(), STRING.next(), PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), false,null);

        setupMockedEventStream(logEvent.getHearingId(), this.eventStream, with(new NewModelHearingAggregate(), a -> {
            a.apply(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));
            a.apply(new HearingEventLogged(
                    logEvent.getHearingEventId(),
                    null,
                    logEvent.getHearingId(),
                    logEvent.getHearingEventDefinitionId(),
                    logEvent.getRecordedLabel(),
                    logEvent.getEventTime(),
                    logEvent.getLastModifiedTime(),
                    logEvent.getAlterable(),
                    initiateHearingCommand.getHearing().getCourtCentreId(),
                    initiateHearingCommand.getHearing().getCourtCentreName(),
                    initiateHearingCommand.getHearing().getCourtRoomId(),
                    initiateHearingCommand.getHearing().getCourtRoomName(),
                    initiateHearingCommand.getHearing().getType(),
                    initiateHearingCommand.getCases().get(0).getUrn(),
                    initiateHearingCommand.getCases().get(0).getCaseId(),
                    logEvent.getWitnessId()));
        }));

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.log-hearing-event"),
                objectToJsonObjectConverter.convert(logEvent));

        hearingEventCommandHandler.logHearingEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.hearing-event-ignored"),
                        payloadIsJson(allOf(
                                withJsonPath("$.alterable", is(logEvent.getAlterable())),
                                withJsonPath("$.hearingEventDefinitionId", is(logEvent.getHearingEventDefinitionId().toString())),
                                withJsonPath("$.hearingEventId", is(logEvent.getHearingEventId().toString())),
                                withJsonPath("$.hearingId", is(logEvent.getHearingId().toString())),
                                withJsonPath("$.recordedLabel", is(logEvent.getRecordedLabel())),
                                withJsonPath("$.eventTime", is(logEvent.getEventTime().toLocalDateTime().atZone(ZoneId.of("Z")).toString()))
                        ))
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void correctHearingEvent_shouldDeleteOldEventAndAddANewEvent() throws Exception {
        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        LogEventCommand logEvent = new LogEventCommand(randomUUID(), initiateHearingCommand.getHearing().getId(),
                randomUUID(), STRING.next(), PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), false,null);

        CorrectLogEventCommand correctLogEvent = new CorrectLogEventCommand(logEvent.getHearingEventId(), randomUUID(), initiateHearingCommand.getHearing().getId(),
                randomUUID(), STRING.next(), PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), false);

        setupMockedEventStream(logEvent.getHearingId(), this.eventStream, with(new NewModelHearingAggregate(), a -> {
            a.apply(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));
            a.apply(new HearingEventLogged(
                    logEvent.getHearingEventId(),
                    null,
                    logEvent.getHearingId(),
                    logEvent.getHearingEventDefinitionId(),
                    logEvent.getRecordedLabel(),
                    logEvent.getEventTime(),
                    logEvent.getLastModifiedTime(),
                    logEvent.getAlterable(),
                    initiateHearingCommand.getHearing().getCourtCentreId(),
                    initiateHearingCommand.getHearing().getCourtCentreName(),
                    initiateHearingCommand.getHearing().getCourtRoomId(),
                    initiateHearingCommand.getHearing().getCourtRoomName(),
                    initiateHearingCommand.getHearing().getType(),
                    initiateHearingCommand.getCases().get(0).getUrn(),
                    initiateHearingCommand.getCases().get(0).getCaseId(),
                    null));
        }));

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.command.correct-hearing-event"),
                objectToJsonObjectConverter.convert(correctLogEvent));

        hearingEventCommandHandler.correctEvent(command);

        List<Object> events = verifyAppendAndGetArgumentFrom(eventStream).collect(Collectors.toList());

        assertThat((JsonEnvelope) events.get(0),

                jsonEnvelope(
                        withMetadataEnvelopedFrom(command).withName("hearing.hearing-event-deleted"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingEventId", is(logEvent.getHearingEventId().toString()))
                        ))
                ).thatMatchesSchema()
        );

        assertThat((JsonEnvelope) events.get(1),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command).withName("hearing.hearing-event-logged"),
                        payloadIsJson(allOf(
                                withJsonPath("$.alterable", is(correctLogEvent.getAlterable())),
                                withJsonPath("$.hearingEventDefinitionId", is(correctLogEvent.getHearingEventDefinitionId().toString())),
                                withJsonPath("$.hearingEventId", is(correctLogEvent.getLatestHearingEventId().toString())),
                                withJsonPath("$.hearingId", is(correctLogEvent.getHearingId().toString())),
                                withJsonPath("$.recordedLabel", is(correctLogEvent.getRecordedLabel())),
                                withJsonPath("$.eventTime", is(correctLogEvent.getEventTime().toLocalDateTime().atZone(ZoneId.of("Z")).toString())),
                                withJsonPath("$.lastHearingEventId", is(correctLogEvent.getHearingEventId().toString())),
                                withJsonPath("$.lastModifiedTime", is(correctLogEvent.getLastModifiedTime().toLocalDateTime().atZone(ZoneId.of("Z")).toString())),
                                withJsonPath("$.caseId", is(initiateHearingCommand.getCases().get(0).getCaseId().toString())),
                                withJsonPath("$.caseUrn", is(initiateHearingCommand.getCases().get(0).getUrn())),
                                withJsonPath("$.courtCentreId", is(initiateHearingCommand.getHearing().getCourtCentreId().toString())),
                                withJsonPath("$.courtCentreName", is(initiateHearingCommand.getHearing().getCourtCentreName())),
                                withJsonPath("$.courtRoomId", is(initiateHearingCommand.getHearing().getCourtRoomId().toString())),
                                withJsonPath("$.courtRoomName", is(initiateHearingCommand.getHearing().getCourtRoomName())),
                                withJsonPath("$.hearingType", is(initiateHearingCommand.getHearing().getType()))
                        ))
                ).thatMatchesSchema()
        );
    }

    @Test
    public void correctHearingEvent_shouldIgnoreCorrection_givenNoPreviousEventFound() throws Exception {
        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        CorrectLogEventCommand correctLogEvent = new CorrectLogEventCommand(randomUUID(), randomUUID(), initiateHearingCommand.getHearing().getId(),
                randomUUID(), STRING.next(), PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), false);

        setupMockedEventStream(correctLogEvent.getHearingId(), this.eventStream, with(new NewModelHearingAggregate(), a -> {
            a.apply(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));
        }));

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.command.correct-hearing-event"),
                objectToJsonObjectConverter.convert(correctLogEvent));

        hearingEventCommandHandler.correctEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.hearing-event-ignored"),
                        payloadIsJson(allOf(
                                withJsonPath("$.alterable", is(correctLogEvent.getAlterable())),
                                withJsonPath("$.hearingEventDefinitionId", is(correctLogEvent.getHearingEventDefinitionId().toString())),
                                withJsonPath("$.hearingEventId", is(correctLogEvent.getHearingEventId().toString())),
                                withJsonPath("$.hearingId", is(correctLogEvent.getHearingId().toString())),
                                withJsonPath("$.recordedLabel", is(correctLogEvent.getRecordedLabel())),
                                withJsonPath("$.eventTime", is(correctLogEvent.getEventTime().toLocalDateTime().atZone(ZoneId.of("Z")).toString()))
                        ))
                ).thatMatchesSchema()
        ));
    }

    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}