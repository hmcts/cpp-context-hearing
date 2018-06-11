package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.prosecutionCounsel.AddProsecutionCounselCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class AddProsecutionCounselCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AddProsecutionCounselCommandHandler.class.getName());

    @Handles("hearing.add-prosecution-counsel")
    public void addProsecutionCounsel(final JsonEnvelope envelope) throws EventStreamException {

        LOGGER.debug("hearing.add-prosecution-counsel event received {}", envelope.payloadAsJsonObject());

        final AddProsecutionCounselCommand addProsecutionCounselCommand = convertToObject(envelope, AddProsecutionCounselCommand.class);

        aggregate(NewModelHearingAggregate.class, addProsecutionCounselCommand.getHearingId(), envelope,
                (hearingAggregate) -> hearingAggregate.addProsecutionCounsel(addProsecutionCounselCommand));
    }
}
