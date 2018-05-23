package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingEventDefinitionAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(COMMAND_HANDLER)
public class HearingEventCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HearingEventCommandHandler.class.getName());

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
    private static final String FIELD_HEARING_ID = "hearingId";
    @Inject
    public HearingEventCommandHandler(final EventSource eventSource, final Enveloper enveloper, final AggregateService aggregateService, final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        super(eventSource, enveloper, aggregateService, jsonObjectToObjectConverter);
    }

    @Handles("hearing.create-hearing-event-definitions")
    public void createHearingEventDefinitions(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.create-hearing-event-definitions event received {}", envelope.payloadAsJsonObject());

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
        LOGGER.debug("hearing.command.log-hearing-event event received {}", command.payloadAsJsonObject());

        final LogEventCommand logEventCommand = this.jsonObjectToObjectConverter.convert(
                command.payloadAsJsonObject(), LogEventCommand.class);

        aggregate(NewModelHearingAggregate.class, logEventCommand.getHearingId(), command, a -> a.logHearingEvent(logEventCommand));
    }

    @Handles("hearing.command.update-hearing-events")
    public void updateHearingEvents(final JsonEnvelope command) throws EventStreamException {
      final JsonObject payload = command.payloadAsJsonObject();  
        aggregate(NewModelHearingAggregate.class, fromString(payload.getString(FIELD_HEARING_ID)),
                        command,
                        a -> a.updateHearingEvents(payload));
    }

    @Handles("hearing.command.correct-hearing-event")
    public void correctEvent(final JsonEnvelope command) throws EventStreamException {
        LOGGER.debug("hearing.command.correct-hearing-event event received {}", command.payloadAsJsonObject());

        final CorrectLogEventCommand logEventCommand = this.jsonObjectToObjectConverter.convert(
                command.payloadAsJsonObject(), CorrectLogEventCommand.class);

        aggregate(NewModelHearingAggregate.class, logEventCommand.getHearingId(), command, a -> a.correctHearingEvent(logEventCommand));
    }
}
