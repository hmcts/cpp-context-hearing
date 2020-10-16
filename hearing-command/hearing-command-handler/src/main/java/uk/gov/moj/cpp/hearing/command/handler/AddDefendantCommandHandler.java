package uk.gov.moj.cpp.hearing.command.handler;

import static java.lang.String.format;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.handler.exception.HearingNotFoundException;
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
        final List<Defendant> defendants = convertToList(envelope.payloadAsJsonObject().getJsonArray("defendants"), Defendant.class);
        for (final Defendant defendant : defendants) {
            List<UUID> hearingsLinkedWithCase = aggregate(CaseAggregate.class, defendant.getProsecutionCaseId()).getHearingIds();
            // Currently in production on rare occasion, add defendant occurs before the case arrived to hearing.
            // when this happens, the current implementation line 35 returns empty list and hence no event has been raised to add defendant to the case.
            // To avoid this problem we are raising an exception so that the command message will be DLQ to be processed later with auto retry mechanism
            // Associated JIRA ticket DD-4019
            if (isEmpty(hearingsLinkedWithCase)) {
                throw new HearingNotFoundException(format("Defendant '%s' can't be added to Prosecution Case '%s' ", defendant.getId(), defendant.getProsecutionCaseId()));
            }
            for (final UUID hearingId : hearingsLinkedWithCase) {
                aggregate(HearingAggregate.class, hearingId, envelope, a -> a.addDefendant(hearingId, defendant));
            }
        }
    }

}
