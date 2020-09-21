package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.List;
import java.util.UUID;

import javax.json.JsonString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class RemoveTargetsCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RemoveTargetsCommandHandler.class.getName());

    @Handles("hearing.command.remove-targets")
    public void removeTargets(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.remove-targets event received {}", envelope.toObfuscatedDebugString());
        }
        final List<String> targetIds = envelope.payloadAsJsonObject().getJsonArray("targetIds").getValuesAs(JsonString.class).stream().map(JsonString::getString).collect(toList());
        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString("hearingId"));
        for (final String targetId : targetIds) {
            aggregate(HearingAggregate.class, hearingId, envelope, a -> a.removeTarget(hearingId, UUID.fromString(targetId)));
        }
    }
}
