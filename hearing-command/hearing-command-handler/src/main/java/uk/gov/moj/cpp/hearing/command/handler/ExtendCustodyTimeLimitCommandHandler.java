package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import java.time.LocalDate;
import java.util.UUID;

@ServiceComponent(COMMAND_HANDLER)
public class ExtendCustodyTimeLimitCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendCustodyTimeLimitCommandHandler.class);

    private static final String HEARING_ID = "hearingId";
    private static final String OFFENCE_ID = "offenceId";
    private static final String EXTENDED_LIMIT_LIMIT = "extendedTimeLimit";

    @Handles("hearing.command.extend-custody-time-limit")
    public void extendCustodyTimeLimit(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'hearing.command.extend-custody-time-limit' received with payload {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final UUID offenceId = UUID.fromString(envelope.payloadAsJsonObject().getString(OFFENCE_ID));
        final LocalDate extendedTimeLimit = LocalDate.parse(envelope.payloadAsJsonObject().getString(EXTENDED_LIMIT_LIMIT));

        aggregate(HearingAggregate.class, hearingId, envelope, aggregate -> aggregate.extendCustodyTimeLimit(hearingId, offenceId, extendedTimeLimit));

    }

}
