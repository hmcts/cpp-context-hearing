package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class RequestApprovalCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RequestApprovalCommandHandler.class.getName());

    @Handles("hearing.command.request-approval")
    public void requestApproval(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.request-approval event received {}", envelope.toObfuscatedDebugString());
        }
        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString("hearingId"));
        final Optional<String> userId = envelope.metadata().userId();
        if (userId.isPresent()) {
            aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate -> hearingAggregate.approvalRequest(hearingId, UUID.fromString(userId.get())));
        }
    }

    @Handles("hearing.command.change-cancel-amendments")
    @SuppressWarnings("squid:S3655")
    public void cancelAmendments(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.change-cancel-amendments {}", envelope.toObfuscatedDebugString());
        }
        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString("hearingId"));
        final UUID userId = UUID.fromString(envelope.metadata().userId().get());
        final boolean resetHearing = envelope.payloadAsJsonObject().getBoolean("resetHearing",false);
        aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate -> hearingAggregate.cancelAmendmentsSincePreviousShare(hearingId, userId,resetHearing));
    }

}

