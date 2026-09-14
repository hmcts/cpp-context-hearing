package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class ReleaseProvisionalHearingSlotsCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseProvisionalHearingSlotsCommandHandler.class.getName());

    @Handles("hearing.command.release-provisional-hearing-slots")
    public void releaseProvisionalHearingSlots(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.release-provisional-hearing-slots received {}", envelope.toObfuscatedDebugString());
        }
        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString("hearingId"));
        final String bookingId = envelope.payloadAsJsonObject().getString("bookingId");

        aggregate(HearingAggregate.class, hearingId, envelope,
                hearingAggregate -> hearingAggregate.releaseProvisionalHearingSlots(hearingId, bookingId));
    }
}
