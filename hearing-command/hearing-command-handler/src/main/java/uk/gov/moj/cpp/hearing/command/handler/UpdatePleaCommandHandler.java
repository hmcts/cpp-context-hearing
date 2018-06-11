package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.plea.Defendant;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.plea.Offence;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class UpdatePleaCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UpdatePleaCommandHandler.class.getName());

    @Handles("hearing.hearing-offence-plea-update")
    public void updatePlea(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.hearing-offence-plea-update event received {}", envelope.payloadAsJsonObject());

        final HearingUpdatePleaCommand command = convertToObject(envelope, HearingUpdatePleaCommand.class);
        for (final Defendant defendant : command.getDefendants()) {
            for (final Offence offence : defendant.getOffences()) {
                aggregate(NewModelHearingAggregate.class, command.getHearingId(), envelope,
                        (hearingAggregate) -> hearingAggregate.updatePlea(command.getHearingId(), offence.getId(),
                                offence.getPlea().getPleaDate(), offence.getPlea().getValue()));
            }
        }
    }

    @Handles("hearing.offence-plea-updated")
    public void updateOffencePlea(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.offence-plea-updated event received {}", envelope.payloadAsJsonObject());

        final OffencePleaUpdated event = convertToObject(envelope, OffencePleaUpdated.class);
        aggregate(OffenceAggregate.class, event.getOffenceId(), envelope,
                (offenceAggregate) -> offenceAggregate.updatePlea(event.getHearingId(), event.getOffenceId(), event.getPleaDate(), event.getValue()));
    }
}