package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import java.util.UUID;

@ServiceComponent(COMMAND_HANDLER)
public class AddMasterDefendantIdToDefendantCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AddMasterDefendantIdToDefendantCommandHandler.class.getName());

    @Handles("hearing.command.add-master-defendant-id-to-defendant")
    public void addMasterDefendantIdToDefendant(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.add-master-defendant-id-to-defendant {}", envelope.toObfuscatedDebugString());
        }
        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString("hearingId"));
        final UUID prosecutionCaseId = UUID.fromString(envelope.payloadAsJsonObject().getString("prosecutionCaseId"));
        final UUID defendantId = UUID.fromString(envelope.payloadAsJsonObject().getString("defendantId"));
        final UUID masterDefendantId = UUID.fromString(envelope.payloadAsJsonObject().getString("masterDefendantId"));
        aggregate(HearingAggregate.class, hearingId, envelope, a -> a.addMasterDefendantIdToDefendant(hearingId, prosecutionCaseId, defendantId, masterDefendantId));
    }
}
