package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.UUID;

import jakarta.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class UpdateHearingDayBdfCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateHearingDayBdfCommandHandler.class);

    private static final String HEARING_ID = "hearingId";
    private static final String HEARING_DAY = "hearingDay";

    @Handles("hearing.command.update-hearing-day-bdf")
    public void updateHearingDayBdf(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'hearing.command.update-hearing-day-bdf' received with payload {}", envelope.toObfuscatedDebugString());
        }

        final JsonObject payload = envelope.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(HEARING_ID));
        final HearingDay hearingDay = convertToObject(payload.getJsonObject(HEARING_DAY), HearingDay.class);

        aggregate(HearingAggregate.class,
                hearingId,
                envelope,
                hearingAggregate -> hearingAggregate.updateHearingDayBdf(hearingId, hearingDay));
    }
}
