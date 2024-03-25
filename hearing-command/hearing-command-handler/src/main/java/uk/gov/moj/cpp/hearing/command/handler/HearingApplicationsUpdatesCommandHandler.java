package uk.gov.moj.cpp.hearing.command.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingApplicationsTobeAddedCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class HearingApplicationsUpdatesCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HearingApplicationsUpdatesCommandHandler.class.getName());

    @Handles("hearing.command.breach-applications-to-be-added")
    public void recordApplicationsToBeAddedToHearing(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.breach-applications-to-be-added {}", envelope.payloadAsJsonObject());
        }
        final HearingApplicationsTobeAddedCommand hearingApplicationsTobeAddedCommand = convertToObject(envelope, HearingApplicationsTobeAddedCommand.class);
        aggregate(HearingAggregate.class, hearingApplicationsTobeAddedCommand.getHearingId(), envelope, a -> a.receiveBreachApplicationToBeAdded(
                hearingApplicationsTobeAddedCommand.getBreachedApplications()

        ));
    }


}

