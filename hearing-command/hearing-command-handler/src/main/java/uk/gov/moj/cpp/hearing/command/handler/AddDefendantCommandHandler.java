package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.hearing.courts.HearingAddDefendants;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class AddDefendantCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AddDefendantCommandHandler.class.getName());

    @Handles("hearing.add-defendants")
    public void addDefendant(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.add-defendant event received {}", envelope.toObfuscatedDebugString());
        }

        final HearingAddDefendants hearingAddDefendants = convertToObject(envelope.payloadAsJsonObject(), HearingAddDefendants.class);

        for (final Defendant defendant : hearingAddDefendants.getDefendants()) {
            final List<UUID> hearingsLinkedWithCase = aggregate(CaseAggregate.class, defendant.getProsecutionCaseId()).getHearingIds();

            for (final UUID hearingId : hearingsLinkedWithCase) {
                aggregate(HearingAggregate.class, hearingId, envelope, a -> a.addDefendant(hearingId, defendant, hearingAddDefendants.getListHearingRequests()));
            }
        }
    }
}
