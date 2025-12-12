package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class JudiciaryUpdatedUserAddedCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JudiciaryUpdatedUserAddedCommandHandler.class.getName());

    @Handles("hearing.command.user-attached-to-judiciary")
    public void recordNextHearingDayUpdated(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.user-attached-to-judiciary {}", envelope.toObfuscatedDebugString());
        }

        final UUID judiciaryId = UUID.fromString(envelope.payloadAsJsonObject().getString("judiciaryId"));
        final String emailId = envelope.payloadAsJsonObject().getString("emailId");
        final UUID cpUserId = UUID.fromString(envelope.payloadAsJsonObject().getString("cpUserId"));
        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString("hearingId"));
        final UUID id = UUID.fromString(envelope.payloadAsJsonObject().getString("id"));

        aggregate(HearingAggregate.class, hearingId, envelope, a -> a.userAddedToJudiciary(
                judiciaryId,
                emailId,
                cpUserId,
                hearingId,
                id
        ));
    }

}
