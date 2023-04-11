package uk.gov.moj.cpp.hearing.command.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.UUID;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class CustodyTimeLimitClockHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustodyTimeLimitClockHandler.class);

    private static final String HEARING_ID = "hearingId";


    @Handles("hearing.command.stop-custody-time-limit-clock")
    public void stopCustodyTimeLimitClock(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'hearing.command.extend-custody-time-limit' received with payload {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));

        aggregate(HearingAggregate.class, hearingId, envelope, HearingAggregate :: stopCustodyTimeLimitClock);

    }
}
