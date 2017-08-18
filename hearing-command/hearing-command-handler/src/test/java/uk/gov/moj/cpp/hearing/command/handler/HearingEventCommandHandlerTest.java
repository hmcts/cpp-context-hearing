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
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromString;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_UTC_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingEventDefinitionAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingEventLogAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeletionIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;

import java.util.UUID;

import javax.json.JsonArrayBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings({"unused", "unchecked"})
@RunWith(MockitoJUnitRunner.class)
public class HearingEventCommandHandlerTest {

    private static final String LOG_HEARING_EVENT_COMMAND = "hearing.log-hearing-event";
    private static final String HEARING_CORRECT_EVENT_COMMAND = "hearing.correct-hearing-event";

    private static final String HEARING_EVENT_LOGGED_EVENT = "hearing.hearing-event-logged";
    private static final String HEARING_EVENT_DELETED_EVENT = "hearing.hearing-event-deleted";
    private static final String HEARING_EVENT_DEFINITIONS_CREATED_EVENT = "hearing.hearing-event-definitions-created";
    private static final String HEARING_EVENT_DEFINITIONS_DELETED_EVENT = "hearing.hearing-event-definitions-deleted";
    private static final String HEARING_EVENT_DELETION_IGNORED_EVENT = "hearing.hearing-event-deletion-ignored";
    private static final String HEARING_EVENT_IGNORED_EVENT = "hearing.hearing-event-ignored";

    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_LATEST_HEARING_EVENT_ID = "latestHearingEventId";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";

    private static final String FIELD_ACTION_LABEL = "actionLabel";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_SEQUENCE = "sequence";
    private static final String FIELD_SEQUENCE_TYPE = "sequenceType";
    private static final String FIELD_CASE_ATTRIBUTE = "caseAttribute";
    private static final String FIELD_ALTERABLE = "alterable";
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String FIELD_EVENT_DEFINITIONS = "eventDefinitions";
    private static final String FIELD_GROUP_LABEL = "groupLabel";
    private static final String FIELD_ACTION_LABEL_EXTENSION = "actionLabelExtension";

    private static final String FIELD_REASON = "reason";

    private static final UUID HEARING_ID = randomUUID();

    private static final UUID HEARING_EVENT_ID = randomUUID();
    private static final UUID LATEST_HEARING_EVENT_ID = randomUUID();
    private static final String EVENT_TIME = ZonedDateTimes.toString(PAST_UTC_DATE_TIME.next());
    private static final String LAST_MODIFIED_TIME = ZonedDateTimes.toString(PAST_UTC_DATE_TIME.next());
    private static final String DIFFERENT_EVENT_TIME = ZonedDateTimes.toString(PAST_UTC_DATE_TIME.next());
    private static final String DIFFERENT_LAST_MODIFIED_TIME = ZonedDateTimes.toString(PAST_UTC_DATE_TIME.next());

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
            HearingEventDeletionIgnored.class, HearingEventDeleted.class, HearingEventIgnored.class,
            HearingEventDefinitionsDeleted.class);

    @InjectMocks
    private HearingEventCommandHandler hearingEventCommandHandler;

    @Before
    public void setup() {
        when(eventSource.getStreamById(HEARING_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, HearingEventLogAggregate.class)).thenReturn(new HearingEventLogAggregate());
    }

    @Test
    public void shouldAlwaysRaiseHearingEventDefinitionsDeletedAndCreatedEvents() throws Exception {
        when(eventSource.getStreamById(HEARING_EVENT_DEFINITIONS_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, HearingEventDefinitionAggregate.class)).thenReturn(new HearingEventDefinitionAggregate());

        final JsonEnvelope command = createHearingEventDefinitions();

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
    public void shouldRaiseHearingEventLoggedIfNotAlreadyLogged() throws Exception {
        final JsonEnvelope command = createLogHearingEventCommand();

        hearingEventCommandHandler.logHearingEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_LOGGED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_ALTERABLE), equalTo(ALTERABLE)),
                                withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                                withJsonPath(format("$.%s", FIELD_EVENT_TIME), is(EVENT_TIME)),
                                withJsonPath(format("$.%s", FIELD_LAST_MODIFIED_TIME), is(LAST_MODIFIED_TIME))
                        ))
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldIgnoreLogHearingEventIfItsAlreadyBeenLogged() throws Exception {
        final HearingEventLogAggregate hearingEventLogAggregate = new HearingEventLogAggregate();
        hearingEventLogAggregate.apply(new HearingEventLogged(HEARING_EVENT_ID, HEARING_ID, HEARING_EVENT_DEFINITION_ID, RECORDED_LABEL, fromString(EVENT_TIME), fromString(LAST_MODIFIED_TIME), ALTERABLE));
        when(aggregateService.get(eventStream, HearingEventLogAggregate.class)).thenReturn(hearingEventLogAggregate);

        final JsonEnvelope command = createLogHearingEventCommand();

        hearingEventCommandHandler.logHearingEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_IGNORED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_ALTERABLE), equalTo(ALTERABLE)),
                                withJsonPath(format("$.%s", FIELD_HEARING_EVENT_DEFINITION_ID), equalTo(HEARING_EVENT_DEFINITION_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                                withJsonPath(format("$.%s", FIELD_EVENT_TIME), is(EVENT_TIME)),
                                withoutJsonPath(format("$.%s", FIELD_LAST_MODIFIED_TIME)),
                                withJsonPath(format("$.%s", FIELD_REASON), equalTo("Already logged"))
                        ))
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldIgnoreLogHearingEventIfItsBeenDeleted() throws Exception {
        final HearingEventLogAggregate hearingEventLogAggregate = new HearingEventLogAggregate();
        hearingEventLogAggregate.apply(new HearingEventDeleted(HEARING_EVENT_ID));
        when(aggregateService.get(eventStream, HearingEventLogAggregate.class)).thenReturn(hearingEventLogAggregate);

        final JsonEnvelope command = createLogHearingEventCommand();

        hearingEventCommandHandler.logHearingEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_IGNORED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_ALTERABLE), equalTo(ALTERABLE)),
                                withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                                withJsonPath(format("$.%s", FIELD_EVENT_TIME), is(EVENT_TIME)),
                                withoutJsonPath(format("$.%s", FIELD_LAST_MODIFIED_TIME)),
                                withJsonPath(format("$.%s", FIELD_REASON), equalTo("Already deleted"))
                        ))
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseLoggedAndDeletedHearingEventsWhenEventTimeOfExistingHearingEventIsCorrected() throws Exception {
        final HearingEventLogAggregate hearingEventLogAggregate = new HearingEventLogAggregate();
        hearingEventLogAggregate.apply(new HearingEventLogged(HEARING_EVENT_ID, HEARING_ID, HEARING_EVENT_DEFINITION_ID, RECORDED_LABEL, fromString(EVENT_TIME), fromString(LAST_MODIFIED_TIME), ALTERABLE));
        when(aggregateService.get(eventStream, HearingEventLogAggregate.class)).thenReturn(hearingEventLogAggregate);

        final JsonEnvelope command = createCorrectHearingEventCommand();
        hearingEventCommandHandler.correctEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_LOGGED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_EVENT_DEFINITION_ID), equalTo(HEARING_EVENT_DEFINITION_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(LATEST_HEARING_EVENT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                                withJsonPath(format("$.%s", FIELD_EVENT_TIME), is(DIFFERENT_EVENT_TIME)),
                                withJsonPath(format("$.%s", FIELD_LAST_MODIFIED_TIME), is(DIFFERENT_LAST_MODIFIED_TIME))
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
    public void shouldLogUpdatedEventAndIgnoreDeletionWhenEventTimeIsCorrectedForHearingEventWhichHasNotBeenLogged() throws Exception {
        final JsonEnvelope command = createCorrectHearingEventCommand();
        hearingEventCommandHandler.correctEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_LOGGED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(LATEST_HEARING_EVENT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                                withJsonPath(format("$.%s", FIELD_EVENT_TIME), is(DIFFERENT_EVENT_TIME)),
                                withJsonPath(format("$.%s", FIELD_LAST_MODIFIED_TIME), is(DIFFERENT_LAST_MODIFIED_TIME))
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

    private static JsonEnvelope createHearingEventDefinitions() {
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

        return envelopeFrom(metadataWithRandomUUIDAndName(),
                createObjectBuilder()
                        .add(FIELD_GENERIC_ID, HEARING_EVENT_DEFINITIONS_ID.toString())
                        .add(FIELD_EVENT_DEFINITIONS, eventDefinitionsBuilder)
                        .build());
    }

    private JsonEnvelope createLogHearingEventCommand() {
        return envelope()
                .with(metadataWithRandomUUID(LOG_HEARING_EVENT_COMMAND))
                .withPayloadOf(ALTERABLE, FIELD_ALTERABLE)
                .withPayloadOf(HEARING_EVENT_DEFINITION_ID.toString(), FIELD_HEARING_EVENT_DEFINITION_ID)
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(HEARING_EVENT_ID, FIELD_HEARING_EVENT_ID)
                .withPayloadOf(RECORDED_LABEL, FIELD_RECORDED_LABEL)
                .withPayloadOf(EVENT_TIME, FIELD_EVENT_TIME)
                .withPayloadOf(LAST_MODIFIED_TIME, FIELD_LAST_MODIFIED_TIME)
                .build();
    }

    private JsonEnvelope createCorrectHearingEventCommand() {
        return getBasicCorrectHearingEventCommandBuilder()
                .withPayloadOf(HEARING_EVENT_DEFINITION_ID.toString(), FIELD_HEARING_EVENT_DEFINITION_ID)
                .build();
    }

    private JsonEnvelope createCorrectHearingEventCommandWithoutDefinitionId() {
        return getBasicCorrectHearingEventCommandBuilder()
                .build();
    }

    private JsonEnvelopeBuilder getBasicCorrectHearingEventCommandBuilder() {
        return envelope()
                .with(metadataWithRandomUUID(HEARING_CORRECT_EVENT_COMMAND))
                .withPayloadOf(ALTERABLE, FIELD_ALTERABLE)
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(HEARING_EVENT_ID, FIELD_HEARING_EVENT_ID)
                .withPayloadOf(RECORDED_LABEL, FIELD_RECORDED_LABEL)
                .withPayloadOf(LATEST_HEARING_EVENT_ID, FIELD_LATEST_HEARING_EVENT_ID)
                .withPayloadOf(DIFFERENT_EVENT_TIME, FIELD_EVENT_TIME)
                .withPayloadOf(DIFFERENT_LAST_MODIFIED_TIME, FIELD_LAST_MODIFIED_TIME);
    }
}