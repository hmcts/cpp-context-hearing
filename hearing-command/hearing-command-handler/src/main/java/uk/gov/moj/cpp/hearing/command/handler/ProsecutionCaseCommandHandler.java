package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.hearing.courts.UpdateCpsProsecutorWithHearings;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class ProsecutionCaseCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ProsecutionCaseCommandHandler.class.getName());

    @Handles("hearing.command.update-cps-prosecutor-with-associated-hearings")
    public void updateProsecutorForAssociatedHearings(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-cps-prosecutor-with-associated-hearings event received {}", envelope.toObfuscatedDebugString());
        }

        final UpdateCpsProsecutorWithHearings updateCpsProsecutorWithHearings = convertToObject(envelope, UpdateCpsProsecutorWithHearings.class);

        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                .withProsecutionAuthorityId(updateCpsProsecutorWithHearings.getProsecutionAuthorityId())
                .withProsecutionAuthorityCode(updateCpsProsecutorWithHearings.getProsecutionAuthorityCode())
                .withProsecutionAuthorityName(updateCpsProsecutorWithHearings.getProsecutionAuthorityName())
                .withProsecutionAuthorityReference(updateCpsProsecutorWithHearings.getProsecutionAuthorityReference())
                .withCaseURN(updateCpsProsecutorWithHearings.getCaseURN())
                .withAddress(updateCpsProsecutorWithHearings.getAddress())
                .build();

        for (final UUID hearingId : updateCpsProsecutorWithHearings.getHearingIds()) {
            aggregate(HearingAggregate.class, hearingId, envelope,
                    hearingAggregate -> hearingAggregate.updateProsecutor(hearingId, updateCpsProsecutorWithHearings.getProsecutionCaseId(), prosecutionCaseIdentifier));
        }
    }

}
