package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class CancelHearingDaysCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CancelHearingDaysCommandHandler.class.getName());
    private static final String HEARING_ID = "hearingId";

    @Handles("hearing.command.cancel-hearing-days")
    public void cancelHearingDays(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.cancel-hearing-days command received {}", envelope.toObfuscatedDebugString());
        }

        final JsonObject payload = envelope.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(HEARING_ID));
        final List<HearingDay> hearingDaysList = convertToList(envelope.payloadAsJsonObject().getJsonArray("hearingDays"), HearingDay.class);

        aggregate(HearingAggregate.class,
                hearingId,
                envelope,
                hearingAggregate ->
                        hearingAggregate.cancelHearingDays(hearingId, hearingDaysList)
        );
    }

}
