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
public class UnlockHearingCommandHandler extends AbstractCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnlockHearingCommandHandler.class.getName());
    private static final String HEARING_DAY = "hearingDay";

    @Handles("hearing.command.unlock-hearing")
    public void unlockHearing(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.unlock-hearing event received {}", envelope.toObfuscatedDebugString());
        }
        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString("hearingId"));
        final LocalDate hearingDay = envelope.payloadAsJsonObject().containsKey(HEARING_DAY) ? LocalDate.parse(envelope.payloadAsJsonObject().getString(HEARING_DAY)) : null;
        final Optional<String> userId = envelope.metadata().userId();

        if (userId.isPresent()) {
            aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate -> hearingAggregate.unlockHearing(hearingId, hearingDay, UUID.fromString(userId.get())));
        }
    }
}

