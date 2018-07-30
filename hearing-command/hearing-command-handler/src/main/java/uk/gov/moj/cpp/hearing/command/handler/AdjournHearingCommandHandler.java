package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.progression.relist.AdjournHearing;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class AdjournHearingCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AdjournHearingCommandHandler.class.getName());


    @Handles("hearing.adjourn-hearing")
    public void adjournHearing(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.adjourn-hearing event received {}", envelope.toObfuscatedDebugString());
        }

        final AdjournHearing adjournHearing = convertToObject(envelope, AdjournHearing.class);
        aggregate(NewModelHearingAggregate.class, adjournHearing.getRequestedByHearingId(), envelope, a -> a.adjournHearing(adjournHearing));
    }

}

