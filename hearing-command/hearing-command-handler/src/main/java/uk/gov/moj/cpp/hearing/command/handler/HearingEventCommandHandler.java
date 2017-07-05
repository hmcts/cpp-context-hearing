package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromJsonString;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingEventsLogAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;

@ServiceComponent(Component.COMMAND_HANDLER)
public class HearingEventCommandHandler {

    private static final String FIELD_HEARING_ID = "hearingId";

    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_LATEST_HEARING_EVENT_ID = "latestHearingEventId";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";

    private static final String FIELD_HEARING_EVENT_DEFINITIONS_ID = "id";
    private static final String FIELD_EVENT_DEFINITIONS = "eventDefinitions";
    private static final String FIELD_ACTION_LABEL = "actionLabel";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_SEQUENCE = "sequence";
    private static final String FIELD_SEQUENCE_TYPE = "sequenceType";
    private static final String FIELD_CASE_ATTRIBUTE = "caseAttribute";
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
        final UUID hearingEventDefinitionsId = fromString(payload.getString(FIELD_HEARING_EVENT_DEFINITIONS_ID));
        final List<HearingEventDefinition> hearingEventDefinitions = payload
                .getJsonArray(FIELD_EVENT_DEFINITIONS).getValuesAs(JsonObject.class)
                .stream().map(hearingDefinitionJson -> new HearingEventDefinition(
                        hearingDefinitionJson.getString(FIELD_ACTION_LABEL),
                        hearingDefinitionJson.getString(FIELD_RECORDED_LABEL),
                        hearingDefinitionJson.containsKey(FIELD_SEQUENCE) ? hearingDefinitionJson.getInt(FIELD_SEQUENCE) : null,
                        hearingDefinitionJson.getString(FIELD_SEQUENCE_TYPE, null),
                        hearingDefinitionJson.getString(FIELD_CASE_ATTRIBUTE, null),
                        hearingDefinitionJson.getString(FIELD_GROUP_LABEL, null),
                        hearingDefinitionJson.getString(FIELD_ACTION_LABEL_EXTENSION, null)
                )).collect(toList());

        final EventStream eventStream = eventSource.getStreamById(hearingEventDefinitionsId);
        final Stream<Object> events = Stream.of(new HearingEventDefinitionsCreated(hearingEventDefinitionsId, hearingEventDefinitions));
        eventStream.append(events.map(enveloper.withMetadataFrom(envelope)));
    }

    @Handles("hearing.log-hearing-event")
    public void logHearingEvent(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingEventId = fromString(payload.getString(FIELD_HEARING_EVENT_ID));
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String recordedLabel = payload.getString(FIELD_RECORDED_LABEL);
        final ZonedDateTime eventTime = fromJsonString(payload.getJsonString(FIELD_EVENT_TIME));
        final ZonedDateTime lastModifiedTime = fromJsonString(payload.getJsonString(FIELD_LAST_MODIFIED_TIME));

        final EventStream eventStream = eventSource.getStreamById(hearingId);
        final HearingEventsLogAggregate aggregate = aggregateService.get(eventStream, HearingEventsLogAggregate.class);
        final Stream<Object> events = aggregate.logHearingEvent(hearingId, hearingEventId, recordedLabel, eventTime, lastModifiedTime);
        eventStream.append(events.map(enveloper.withMetadataFrom(command)));
    }

    @Handles("hearing.correct-hearing-event")
    public void correctEvent(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final UUID hearingEventId = fromString(payload.getString(FIELD_HEARING_EVENT_ID));
        final String recordedLabel = payload.getString(FIELD_RECORDED_LABEL);
        final ZonedDateTime eventTime = fromJsonString(payload.getJsonString(FIELD_EVENT_TIME));
        final ZonedDateTime lastModifiedTime = fromJsonString(payload.getJsonString(FIELD_LAST_MODIFIED_TIME));
        final UUID latestHearingEventId = fromString(payload.getString(FIELD_LATEST_HEARING_EVENT_ID));

        final EventStream eventStream = eventSource.getStreamById(hearingId);
        final HearingEventsLogAggregate aggregate = aggregateService.get(eventStream, HearingEventsLogAggregate.class);
        final Stream<Object> events = aggregate.correctEvent(hearingId, hearingEventId, recordedLabel, eventTime, lastModifiedTime, latestHearingEventId);
        eventStream.append(events.map(enveloper.withMetadataFrom(command)));
    }

}
