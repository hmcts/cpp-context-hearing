package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.moj.cpp.hearing.domain.updatepleas.Plea;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand;

@ServiceComponent(COMMAND_HANDLER)
public class UpdatePleaCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UpdatePleaCommandHandler.class.getName());

    @Handles("hearing.hearing-offence-plea-update")
    public void updatePlea(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.hearing-offence-plea-update event received {}", envelope.toObfuscatedDebugString());
        }
        final UpdatePleaCommand command = convertToObject(envelope, UpdatePleaCommand.class);
        for (final Plea plea : command.getPleas()) {
                aggregate(HearingAggregate.class, plea.getOriginatingHearingId(), envelope,
                        hearingAggregate -> hearingAggregate.updatePlea(plea.getOriginatingHearingId(), plea.getOffenceId(),
                                plea.getPleaDate(), plea.getValue(), plea.getDelegatedPowers()));
        }
    }

    @Handles("hearing.offence-plea-updated")
    public void updateOffencePlea(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.offence-plea-updated event received {}", envelope.toObfuscatedDebugString());
        }
        final OffencePleaUpdated event = convertToObject(envelope, OffencePleaUpdated.class);
        aggregate(OffenceAggregate.class, event.getOffenceId(), envelope,
                offenceAggregate -> offenceAggregate.updatePlea(event));
    }
}