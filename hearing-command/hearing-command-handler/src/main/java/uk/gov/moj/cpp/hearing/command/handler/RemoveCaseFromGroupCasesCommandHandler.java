package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class RemoveCaseFromGroupCasesCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveCaseFromGroupCasesCommandHandler.class);

    @Handles("hearing.command.remove-case-from-group-cases")
    public void removeCaseFromGroupCases(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.info("{} event received with payload {}",
                "hearing.command.remove-case-from-group-cases",
                envelope.payloadAsJsonObject());

        final UUID groupId = fromString(envelope.payloadAsJsonObject().getString("groupId"));
        final UUID masterCaseId = fromString(envelope.payloadAsJsonObject().getString("masterCaseId"));
        final ProsecutionCase removedCase = convertToObject(envelope.payloadAsJsonObject().getJsonObject("removedCase"), ProsecutionCase.class);
        final ProsecutionCase newGroupMaster = envelope.payloadAsJsonObject().containsKey("newGroupMaster") ?
                convertToObject(envelope.payloadAsJsonObject().getJsonObject("newGroupMaster"), ProsecutionCase.class) : null;

        final List<UUID> hearingIds = aggregate(CaseAggregate.class, masterCaseId).getHearingIds();

        for (final UUID hearingId : hearingIds) {
            aggregate(CaseAggregate.class, removedCase.getId(), envelope,
                    agr -> agr.removeCaseFromGroupCases(hearingId, groupId, removedCase, newGroupMaster));
        }

        if (nonNull(newGroupMaster) && !hearingIds.isEmpty()) {
            for (final UUID hearingId : hearingIds) {
                aggregate(CaseAggregate.class, newGroupMaster.getId(), envelope,
                        agr -> agr.updateMasterCaseForHearing(newGroupMaster.getId(), hearingId));
            }
        }
    }
}
