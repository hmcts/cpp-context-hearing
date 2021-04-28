package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.hearing.courts.RecordNextHearingDayUpdated;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import java.time.ZonedDateTime;
import java.util.UUID;

@ServiceComponent(COMMAND_HANDLER)
public class RecordNextHearingDayUpdatedCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordNextHearingDayUpdatedCommandHandler.class.getName());

    @Handles("hearing.command.record-next-hearing-day-updated")
    public void recordNextHearingDayUpdated(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.record-next-hearing-day-updated {}", envelope.toObfuscatedDebugString());
        }

        final RecordNextHearingDayUpdated recordNextHearingDayUpdated = convertToObject(envelope, RecordNextHearingDayUpdated.class);

        final UUID hearingId = recordNextHearingDayUpdated.getHearingId();
        final UUID seedingHearingId = recordNextHearingDayUpdated.getSeedingHearingId();
        final ZonedDateTime hearingStartDate = recordNextHearingDayUpdated.getHearingStartDate();
        aggregate(HearingAggregate.class, seedingHearingId, envelope, a -> a.changeNextHearingStartDate(hearingId, seedingHearingId, hearingStartDate));
    }

}
