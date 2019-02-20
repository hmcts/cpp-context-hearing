package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.hearing.courts.AddDefenceCounsel;
import uk.gov.justice.hearing.courts.RemoveDefenceCounsel;
import uk.gov.justice.hearing.courts.UpdateDefenceCounsel;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class AddDefenceCounselCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AddDefenceCounselCommandHandler.class.getName());

    @Handles("hearing.command.add-defence-counsel")
    public void addDefenceCounsel(final Envelope<AddDefenceCounsel> envelope) throws EventStreamException {

    if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.add-defence-counsel event received {}", envelope.payload().getDefenceCounsel());
        }
        final DefenceCounsel defenceCounsel = envelope.payload().getDefenceCounsel();
        final UUID hearingId = envelope.payload().getHearingId();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.addDefenceCounsel(defenceCounsel, hearingId));
    }

    @Handles("hearing.command.remove-defence-counsel")
    public void removeDefenceCounsel(final Envelope<RemoveDefenceCounsel> envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.remove-defence-counsel event received {}", envelope.payload().getId());
        }
        final UUID id = envelope.payload().getId();
        final UUID hearingId = envelope.payload().getHearingId();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.removeDefenceCounsel(id, hearingId));
    }

    @Handles("hearing.command.update-defence-counsel")
    public void updateDefenceCounsel(final Envelope<UpdateDefenceCounsel> envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.update-defence-counsel event received {}", envelope.payload().getDefenceCounsel());
        }
        final DefenceCounsel defenceCounsel = envelope.payload().getDefenceCounsel();
        final UUID hearingId = envelope.payload().getHearingId();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.updateDefenceCounsel(defenceCounsel, hearingId));
    }
}
