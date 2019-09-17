package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.core.courts.InterpreterIntermediary;
import uk.gov.justice.hearing.courts.AddInterpreterIntermediary;
import uk.gov.justice.hearing.courts.RemoveInterpreterIntermediary;
import uk.gov.justice.hearing.courts.UpdateInterpreterIntermediary;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class InterpreterIntermediaryCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(InterpreterIntermediaryCommandHandler.class.getName());

    @Handles("hearing.command.add-interpreter-intermediary")
    public void addInterpreterIntermediary(final Envelope<AddInterpreterIntermediary> envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.add-interpreter-intermediary event received {}", envelope.payload().getInterpreterIntermediary());
        }

        final InterpreterIntermediary interpreterIntermediary = envelope.payload().getInterpreterIntermediary();

        final UUID hearingId = envelope.payload().getHearingId();

        aggregate(
                HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.addInterpreterIntermediary(hearingId, interpreterIntermediary));
    }

    @Handles("hearing.command.remove-interpreter-intermediary")
    public void removeInterpreterIntermediary(final Envelope<RemoveInterpreterIntermediary> envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.remove-interpreter-intermediary event received {}", envelope.payload().getId());
        }

        final UUID id = envelope.payload().getId();

        final UUID hearingId = envelope.payload().getHearingId();

        aggregate(
                HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.removeInterpreterIntermediary(id, hearingId));
    }

    @Handles("hearing.command.update-interpreter-intermediary")
    public void updateInterpreterIntermediary(final Envelope<UpdateInterpreterIntermediary> envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.update-interpreter-intermediary event received {}", envelope.payload().getInterpreterIntermediary());
        }

        final InterpreterIntermediary interpreterIntermediary= envelope.payload().getInterpreterIntermediary();

        final UUID hearingId = envelope.payload().getHearingId();

        aggregate(
                HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.updateInterpreterIntermediary(hearingId, interpreterIntermediary));
    }
}
