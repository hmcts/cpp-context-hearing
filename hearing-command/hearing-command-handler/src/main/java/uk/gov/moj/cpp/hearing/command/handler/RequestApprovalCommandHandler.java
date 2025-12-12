package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class RequestApprovalCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RequestApprovalCommandHandler.class.getName());
    private static final String HEARING_ID = "hearingId";
    private static final String HEARING_DAY = "hearingDay";

    @Handles("hearing.command.request-approval")
    public void requestApproval(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.request-approval event received {}", envelope.toObfuscatedDebugString());
        }
        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final LocalDate hearingDay = LocalDate.parse(envelope.payloadAsJsonObject().getString(HEARING_DAY));
        final Integer version = envelope.payloadAsJsonObject().getInt("version");
        final Optional<String> userId = envelope.metadata().userId();
        if (userId.isPresent()) {
            aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate ->
                    hearingAggregate.approvalRequest(hearingId, UUID.fromString(userId.get()), hearingDay, version));
        }
    }

    @Handles("hearing.command.change-cancel-amendments")
    @SuppressWarnings("squid:S3655")
    public void cancelAmendments(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.change-cancel-amendments {}", envelope.toObfuscatedDebugString());
        }
        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final UUID userId = UUID.fromString(envelope.metadata().userId().get());
        final LocalDate hearingDay = envelope.payloadAsJsonObject().containsKey(HEARING_DAY) ? LocalDate.parse(envelope.payloadAsJsonObject().getString(HEARING_DAY)) : null;
        final boolean resetHearing = envelope.payloadAsJsonObject().getBoolean("resetHearing", false);
        aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate -> hearingAggregate.cancelAmendmentsSincePreviousShare(hearingId, userId, resetHearing, hearingDay));
    }

}

