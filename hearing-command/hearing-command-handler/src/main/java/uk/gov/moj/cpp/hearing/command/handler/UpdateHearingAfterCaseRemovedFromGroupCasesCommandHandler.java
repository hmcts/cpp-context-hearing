package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class UpdateHearingAfterCaseRemovedFromGroupCasesCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateHearingAfterCaseRemovedFromGroupCasesCommandHandler.class);

    @Handles("hearing.command.update-hearing-after-case-removed-from-group-cases")
    public void updateHearingAfterCaseRemovedFromGroupCases(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.info("{} event received with payload {}",
                "hearing.command.update-hearing-after-case-removed-from-group-cases",
                envelope.payloadAsJsonObject());

        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString("hearingId"));
        final UUID groupId = fromString(envelope.payloadAsJsonObject().getString("groupId"));
        final ProsecutionCase removedCase = convertToObject(envelope.payloadAsJsonObject().getJsonObject("removedCase"), ProsecutionCase.class);
        final ProsecutionCase newGroupMaster = envelope.payloadAsJsonObject().containsKey("newGroupMaster") ?
                convertToObject(envelope.payloadAsJsonObject().getJsonObject("newGroupMaster"), ProsecutionCase.class) : null;

        updateProsecutionCounsels(envelope, hearingId, groupId, removedCase, newGroupMaster);
        updateProsecutionCases(envelope, hearingId, groupId, removedCase, newGroupMaster);
    }

    private void updateProsecutionCounsels(final JsonEnvelope envelope, final UUID hearingId, final UUID groupId,
                                           final ProsecutionCase removedCase, final ProsecutionCase newGroupMaster) throws EventStreamException {
        final HearingAggregate hearingAggregate = aggregate(HearingAggregate.class, hearingId);
        final Map<UUID, ProsecutionCounsel> prosecutionCounsels = hearingAggregate.getProsecutionCounsels();
        final Map<UUID, UUID> groupAndMaster = hearingAggregate.getGroupAndMaster();

        if (groupAndMaster.containsKey(groupId)) {
            final UUID groupMaster = groupAndMaster.get(groupId);
            for (final ProsecutionCounsel prosecutionCounsel : prosecutionCounsels.values()) {
                if (isNotEmpty(prosecutionCounsel.getProsecutionCases())
                        && prosecutionCounsel.getProsecutionCases().contains(groupMaster)) {
                    aggregate(HearingAggregate.class, hearingId, envelope, agr ->
                            agr.updateProsecutionCounsel(hearingId, prosecutionCounsel, removedCase, newGroupMaster));
                }
            }
        }
    }

    private void updateProsecutionCases(final JsonEnvelope envelope, final UUID hearingId, final UUID groupId,
                                        final ProsecutionCase removedCase, final ProsecutionCase newGroupMaster) throws EventStreamException {
        final HearingAggregate hearingAggregate = aggregate(HearingAggregate.class, hearingId);
        final Map<UUID, UUID> groupAndMaster = hearingAggregate.getGroupAndMaster();

        if (groupAndMaster.containsKey(groupId)) {
            aggregate(HearingAggregate.class, hearingId, envelope, agr ->
                    agr.updateCasesAfterCaseRemovedFromGroupCases(hearingId, groupId, removedCase, newGroupMaster));
        }
    }
}
