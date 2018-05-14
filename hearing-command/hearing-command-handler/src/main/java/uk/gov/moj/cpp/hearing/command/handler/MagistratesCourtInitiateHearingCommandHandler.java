package uk.gov.moj.cpp.hearing.command.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.RecordMagsCourtHearingCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingTransformer;
import uk.gov.moj.cpp.hearing.domain.aggregate.MagistratesCourtHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;

import javax.inject.Inject;
import java.util.List;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class MagistratesCourtInitiateHearingCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MagistratesCourtInitiateHearingCommandHandler.class.getName());

    @Inject
    public MagistratesCourtInitiateHearingCommandHandler(final EventSource eventSource, final Enveloper enveloper,
                                                         final AggregateService aggregateService, final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        super(eventSource, enveloper, aggregateService, jsonObjectToObjectConverter);
    }

    @Handles("hearing.record-sending-sheet-complete")
    public void recordSendingSheetComplete(final JsonEnvelope command) throws EventStreamException {
        LOGGER.debug("hearing.record-sending-sheet-complete event received {}", command.payloadAsJsonObject());

        final SendingSheetCompleted sendingSheetCompleted = jsonObjectToObjectConverter
                .convert(command.payloadAsJsonObject(), SendingSheetCompleted.class);

        aggregate(CaseAggregate.class, sendingSheetCompleted.getHearing().getCaseId(), command,
                aggregate -> aggregate.recordSendingSheetComplete(sendingSheetCompleted));
    }

    @Handles("hearing.record-mags-court-hearing")
    public void recordMagsCourtHearing(final JsonEnvelope command) throws EventStreamException {
        LOGGER.debug("hearing.record-mags-court-hearing event received {}", command.payloadAsJsonObject());

        final List<MagsCourtHearingRecorded> hearings2Initiate = new HearingTransformer()
                .transform(jsonObjectToObjectConverter
                        .convert(command.payloadAsJsonObject(), RecordMagsCourtHearingCommand.class).getHearing());

        for (MagsCourtHearingRecorded magsCourtHearingRecorded : hearings2Initiate) {
            aggregate(MagistratesCourtHearingAggregate.class, magsCourtHearingRecorded.getHearingId(), command,
                    a -> a.initiate(magsCourtHearingRecorded));
        }
    }
}
