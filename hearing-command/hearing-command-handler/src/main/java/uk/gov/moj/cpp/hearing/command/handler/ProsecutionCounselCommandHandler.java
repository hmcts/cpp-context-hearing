package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.hearing.courts.AddProsecutionCounsel;
import uk.gov.justice.hearing.courts.RemoveProsecutionCounsel;
import uk.gov.justice.hearing.courts.UpdateProsecutionCounsel;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class ProsecutionCounselCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ProsecutionCounselCommandHandler.class.getName());

    @Handles("hearing.command.add-prosecution-counsel")
    public void addProsecutionCounsel(final Envelope<AddProsecutionCounsel> envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.add-prosecution-counsel event received {}", envelope.payload().getProsecutionCounsel());
        }
        final ProsecutionCounsel prosecutionCounsel = envelope.payload().getProsecutionCounsel();
        final UUID hearingId = envelope.payload().getHearingId();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.addProsecutionCounsel(prosecutionCounsel, hearingId));
    }

    @Handles("hearing.command.remove-prosecution-counsel")
    public void removeProsecutionCounsel(final Envelope<RemoveProsecutionCounsel> envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.remove-prosecution-counsel event received {}", envelope.payload().getId());
        }
        final UUID id = envelope.payload().getId();
        final UUID hearingId = envelope.payload().getHearingId();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.removeProsecutionCounsel(id, hearingId));
    }

    @Handles("hearing.command.update-prosecution-counsel")
    public void updateProsecutionCounsel(final Envelope<UpdateProsecutionCounsel> envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.update-prosecution-counsel event received {}", envelope.payload().getProsecutionCounsel());
        }
        final ProsecutionCounsel prosecutionCounsel = envelope.payload().getProsecutionCounsel();
        final UUID hearingId = envelope.payload().getHearingId();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.updateProsecutionCounsel(prosecutionCounsel, hearingId));
    }
}
