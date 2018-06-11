package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class AddDefenceCounselCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AddDefenceCounselCommandHandler.class.getName());

    @Handles("hearing.add-defence-counsel")
    public void addDefenceCounsel(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.add-defence-counsel event received {}", envelope.payloadAsJsonObject());

        final AddDefenceCounselCommand addDefenceCounselCommand = convertToObject(envelope, AddDefenceCounselCommand.class);

        aggregate(NewModelHearingAggregate.class, addDefenceCounselCommand.getHearingId(), envelope,
                (hearingAggregate) -> hearingAggregate.addDefenceCounsel(addDefenceCounselCommand));
    }
}
