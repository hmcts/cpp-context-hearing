package uk.gov.moj.cpp.hearing.command.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.moj.cpp.hearing.command.RecordMagsCourtHearingCommand;
import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingsPleaAggregate;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class ProgressionCommandHandler {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ProgressionCommandHandler.class.getName());
    @Inject
    private EventSource eventSource;

    @Inject
    private Enveloper enveloper;

    @Inject
    private AggregateService aggregateService;

    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Handles("hearing.record-sending-sheet-complete")
    public void recordSendingSheetComplete(final JsonEnvelope command) throws EventStreamException {
        LOGGER.trace("hearing.record-sending-sheet-complete command");
        final JsonObject payload = command.payloadAsJsonObject();
        final SendingSheetCompleted sendingSheetCompleted = jsonObjectToObjectConverter.convert(payload, SendingSheetCompleted.class);
        final UUID caseId = sendingSheetCompleted.getHearing().getCaseId();
        applyToCaseHearingAggregate(caseId, aggregate -> aggregate.recordSendingSheetComplete(sendingSheetCompleted), command);
    }

    @Handles("hearing.record-mags-court-hearing")
    public void recordMagsCourtHearing(final JsonEnvelope command) throws EventStreamException {

        LOGGER.trace("hearing.record-mags-court-hearing command");
        final JsonObject payload = command.payloadAsJsonObject();
        final RecordMagsCourtHearingCommand typedCommand = jsonObjectToObjectConverter.convert(payload, RecordMagsCourtHearingCommand.class);

        final UUID caseId = typedCommand.getHearing().getCaseId();

        applyToCaseHearingAggregate(caseId, aggregate -> aggregate.recordMagsCourtHearing(typedCommand.getHearing()), command);

    }
    private void applyToCaseHearingAggregate(final UUID streamId, final Function<HearingsPleaAggregate, Stream<Object>> function,
                                             final JsonEnvelope envelope) throws EventStreamException {
        final EventStream eventStream = this.eventSource.getStreamById(streamId);
        final HearingsPleaAggregate hearingsPleaAggregate = this.aggregateService.get(eventStream, HearingsPleaAggregate.class);
        final Stream<Object> events = function.apply(hearingsPleaAggregate);
        eventStream.append(events.map(this.enveloper.withMetadataFrom(envelope)));

    }

}
