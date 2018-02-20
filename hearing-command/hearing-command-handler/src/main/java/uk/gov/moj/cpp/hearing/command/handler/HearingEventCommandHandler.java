package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromJsonString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingEventDefinitionAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingEventLogAggregate;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(COMMAND_HANDLER)
public class HearingEventCommandHandler {

    private static final String FIELD_HEARING_ID = "hearingId";

    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_LATEST_HEARING_EVENT_ID = "latestHearingEventId";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";

    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_EVENT_DEFINITIONS = "eventDefinitions";
    private static final String FIELD_ACTION_LABEL = "actionLabel";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_SEQUENCE = "sequence";
    private static final String FIELD_SEQUENCE_TYPE = "sequenceType";
    private static final String FIELD_CASE_ATTRIBUTE = "caseAttribute";
    private static final String FIELD_ALTERABLE = "alterable";
    private static final String FIELD_GROUP_LABEL = "groupLabel";
    private static final String FIELD_ACTION_LABEL_EXTENSION = "actionLabelExtension";

    @Inject
    private EventSource eventSource;

    @Inject
    private Enveloper enveloper;

    @Inject
    private AggregateService aggregateService;

    @Handles("hearing.create-hearing-event-definitions")
    public void createHearingEventDefinitions(final JsonEnvelope envelope) throws EventStreamException {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final UUID hearingEventDefinitionsId = fromString(payload.getString(FIELD_GENERIC_ID));
        final List<HearingEventDefinition> hearingEventDefinitions = payload
                .getJsonArray(FIELD_EVENT_DEFINITIONS).getValuesAs(JsonObject.class)
                .stream().map(hearingDefinitionJson -> new HearingEventDefinition(
                        fromString(hearingDefinitionJson.getString(FIELD_GENERIC_ID)),
                        hearingDefinitionJson.getString(FIELD_ACTION_LABEL),
                        hearingDefinitionJson.getString(FIELD_RECORDED_LABEL),
                        hearingDefinitionJson.containsKey(FIELD_SEQUENCE) ? hearingDefinitionJson.getInt(FIELD_SEQUENCE) : null,
                        hearingDefinitionJson.getString(FIELD_SEQUENCE_TYPE, null),
                        hearingDefinitionJson.getString(FIELD_CASE_ATTRIBUTE, null),
                        hearingDefinitionJson.getString(FIELD_GROUP_LABEL, null),
                        hearingDefinitionJson.getString(FIELD_ACTION_LABEL_EXTENSION, null),
                        hearingDefinitionJson.getBoolean(FIELD_ALTERABLE)
                )).collect(toList());

        final EventStream eventStream = eventSource.getStreamById(hearingEventDefinitionsId);
        final HearingEventDefinitionAggregate aggregate = aggregateService.get(eventStream, HearingEventDefinitionAggregate.class);
        final Stream<Object> events = aggregate.createEventDefinitions(hearingEventDefinitionsId, hearingEventDefinitions);
        eventStream.append(events.map(enveloper.withMetadataFrom(envelope)));
    }

    @Handles("hearing.command.log-hearing-event")
    public void logHearingEvent(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingEventId = fromString(payload.getString(FIELD_HEARING_EVENT_ID));
        final UUID hearingEventDefinitionId = fromString(payload.getString(FIELD_HEARING_EVENT_DEFINITION_ID));
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String recordedLabel = payload.getString(FIELD_RECORDED_LABEL);
        final ZonedDateTime eventTime = fromJsonString(payload.getJsonString(FIELD_EVENT_TIME));
        final ZonedDateTime lastModifiedTime = fromJsonString(payload.getJsonString(FIELD_LAST_MODIFIED_TIME));
        final boolean alterable = payload.getBoolean(FIELD_ALTERABLE);

        final EventStream eventStream = eventSource.getStreamById(hearingId);
        final HearingEventLogAggregate aggregate = aggregateService.get(eventStream, HearingEventLogAggregate.class);
        final Stream<Object> events = aggregate.logHearingEvent(hearingId, hearingEventId, hearingEventDefinitionId, recordedLabel, eventTime, lastModifiedTime, alterable);
        eventStream.append(events.map(enveloper.withMetadataFrom(command)));
    }

    @Handles("hearing.command.correct-hearing-event")
    public void correctEvent(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final UUID hearingEventId = fromString(payload.getString(FIELD_HEARING_EVENT_ID));
        final UUID hearingEventDefinitionId = fromString(payload.getString(FIELD_HEARING_EVENT_DEFINITION_ID));
        final String recordedLabel = payload.getString(FIELD_RECORDED_LABEL);
        final ZonedDateTime eventTime = fromJsonString(payload.getJsonString(FIELD_EVENT_TIME));
        final ZonedDateTime lastModifiedTime = fromJsonString(payload.getJsonString(FIELD_LAST_MODIFIED_TIME));
        final UUID latestHearingEventId = fromString(payload.getString(FIELD_LATEST_HEARING_EVENT_ID));
        final boolean alterable = payload.getBoolean(FIELD_ALTERABLE);

        final EventStream eventStream = eventSource.getStreamById(hearingId);
        final HearingEventLogAggregate aggregate = aggregateService.get(eventStream, HearingEventLogAggregate.class);
        final Stream<Object> events = aggregate.correctEvent(hearingId, hearingEventId, hearingEventDefinitionId,
                recordedLabel, eventTime, lastModifiedTime, latestHearingEventId, alterable);
        eventStream.append(events.map(enveloper.withMetadataFrom(command)));
    }

}
