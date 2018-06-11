package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.RecordMagsCourtHearingCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingTransformer;
import uk.gov.moj.cpp.hearing.domain.aggregate.MagistratesCourtHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class MagistratesCourtInitiateHearingCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MagistratesCourtInitiateHearingCommandHandler.class.getName());

    @Handles("hearing.record-sending-sheet-complete")
    public void recordSendingSheetComplete(final JsonEnvelope command) throws EventStreamException {
        LOGGER.debug("hearing.record-sending-sheet-complete event received {}", command.payloadAsJsonObject());

        final SendingSheetCompleted sendingSheetCompleted = convertToObject(command, SendingSheetCompleted.class);

        aggregate(CaseAggregate.class, sendingSheetCompleted.getHearing().getCaseId(), command,
                aggregate -> aggregate.recordSendingSheetComplete(sendingSheetCompleted));
    }

    @Handles("hearing.record-mags-court-hearing")
    public void recordMagsCourtHearing(final JsonEnvelope command) throws EventStreamException {
        LOGGER.debug("hearing.record-mags-court-hearing event received {}", command.payloadAsJsonObject());

        final List<MagsCourtHearingRecorded> hearings2Initiate = new HearingTransformer()
                .transform(convertToObject(command, RecordMagsCourtHearingCommand.class).getHearing());

        for (MagsCourtHearingRecorded magsCourtHearingRecorded : hearings2Initiate) {
            aggregate(MagistratesCourtHearingAggregate.class, magsCourtHearingRecorded.getHearingId(), command,
                    a -> a.initiate(magsCourtHearingRecorded));
        }
    }
}
