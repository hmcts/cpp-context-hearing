package uk.gov.moj.cpp.hearing.command.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.application.SaveApplicationResponseCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class SaveApplicationResponseCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SaveApplicationResponseCommandHandler.class.getName());


    @Handles("hearing.command.save-application-response")
    public void saveApplicationResponse(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.save-application-response command received {}", envelope.toObfuscatedDebugString());
        }
        final SaveApplicationResponseCommand command = convertToObject(envelope, SaveApplicationResponseCommand.class);
        aggregate(HearingAggregate.class, command.getHearingId(), envelope,
                aggregate -> aggregate.courtApplicationResponse(command.getApplicationPartyId(), command.getApplicationResponse()));
    }


}