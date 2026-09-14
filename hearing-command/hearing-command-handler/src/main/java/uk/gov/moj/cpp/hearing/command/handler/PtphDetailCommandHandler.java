package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.ListType;
import uk.gov.moj.cpp.hearing.command.SavePtphDetailCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailDeleted;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailFinalised;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailSaved;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S2629"})
@ServiceComponent(COMMAND_HANDLER)
public class PtphDetailCommandHandler extends AbstractCommandHandler {

    private static final String HEARING_ID = "hearingId";

    private static final Logger LOGGER = LoggerFactory.getLogger(PtphDetailCommandHandler.class.getName());

    @Handles("hearing.command.save-ptph-detail")
    public void savePtphDetail(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.save-ptph-detail received {}", envelope.toObfuscatedDebugString());

        final SavePtphDetailCommand command = convertToObject(envelope, SavePtphDetailCommand.class);
        final String keyReason = resolveKeyReason(command);

        aggregate(HearingAggregate.class, command.getHearingId(), envelope,
                a -> a.savePtphDetail(new PtphDetailSaved(
                        command.getHearingId(),
                        command.getTier() == null ? null : command.getTier().name(),
                        command.getListType() == null ? null : command.getListType().name(),
                        keyReason)));
    }

    @Handles("hearing.command.finalise-ptph-detail")
    public void finalisePtphDetail(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.finalise-ptph-detail received {}", envelope.toObfuscatedDebugString());

        final UUID hearingId = hearingId(envelope);
        aggregate(HearingAggregate.class, hearingId, envelope,
                a -> a.finalisePtphDetail(new PtphDetailFinalised(hearingId)));
    }

    @Handles("hearing.command.delete-ptph-detail")
    public void deletePtphDetail(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.delete-ptph-detail received {}", envelope.toObfuscatedDebugString());

        final UUID hearingId = hearingId(envelope);
        aggregate(HearingAggregate.class, hearingId, envelope,
                a -> a.deletePtphDetail(new PtphDetailDeleted(hearingId)));
    }

    /**
     * A key reason only applies to a fixed-date list type, so anything supplied alongside a
     * flexible one is dropped here rather than stored and later ignored.
     *
     * <p>This deliberately does not reject a fixed list type that arrives without a reason. The
     * aggregate drops that command as {@code hearing.hearing-change-ignored}. Throwing from a
     * command handler would roll back the JMS transaction and dead-letter the hearing's command
     * queue, taking unrelated hearing traffic with it.
     *
     * <p>The rule is not expressed in the JSON schemas either. The command API is REST-only and
     * registers no schema-validation provider, so a violation there is accepted with 202 anyway;
     * and the command-handler schema IS validated, on the JMS queue, where a violation
     * dead-letters the message. The aggregate is the only place that can refuse it safely.
     */
    private String resolveKeyReason(final SavePtphDetailCommand command) {
        return command.getListType() == ListType.TYPE_1_FIXED ? command.getKeyReason() : null;
    }

    private UUID hearingId(final JsonEnvelope envelope) {
        return fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
    }
}
