package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.hearing.courts.CourtListRestricted;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class RestrictCourtListCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestrictCourtListCommandHandler.class.getName());

    @Handles("hearing.command.restrict-court-list")
    public void restrictCourtList(final Envelope<CourtListRestricted> envelope) throws EventStreamException {
        LOGGER.info("hearing.command.restrict-court-list event received for hearingId {}", envelope.payload().getHearingId());

        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                envelope.payload().getHearingId(),
                envelope,
                aggregate -> aggregate.courtListRestrictions(envelope.payload()));
    }
}
