package uk.gov.moj.cpp.hearing.command.handler;


import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.SystemUserProvider;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.enforcement.EnforceFinancialImpositionAcknowledgement;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.SaveNowsVariantsCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PendingNowsRequestedCommand;
import uk.gov.moj.cpp.systemidmapper.client.SystemIdMapperClient;
import uk.gov.moj.cpp.systemidmapper.client.SystemIdMapping;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class GenerateNowsCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GenerateNowsCommandHandler.class.getName());

    @Inject
    private SystemUserProvider systemUserProvider;

    @Inject
    private SystemIdMapperClient systemIdMapperClient;

    @Handles("hearing.command.save-nows-variants")
    public void saveNowsVariants(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.save-nows-variants event received {}", envelope.toObfuscatedDebugString());
        }
        final SaveNowsVariantsCommand saveNowsVariantsCommand = convertToObject(envelope, SaveNowsVariantsCommand.class);
        aggregate(HearingAggregate.class, saveNowsVariantsCommand.getHearingId(), envelope, a -> a.saveNowsVariants(saveNowsVariantsCommand.getHearingId(), saveNowsVariantsCommand.getVariants()));
    }

    @Handles("hearing.command.pending-nows-requested")
    public void pendingNowsRequested(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.pending-nows-requested {}", envelope.toObfuscatedDebugString());
        }

        final PendingNowsRequestedCommand pendingNowsRequestedCommand = convertToObject(envelope, PendingNowsRequestedCommand.class);

        final UUID hearingId = pendingNowsRequestedCommand.getCreateNowsRequest().getHearing().getId();

        aggregate(HearingAggregate.class, hearingId, envelope, aggregate -> aggregate.registerPendingNowsRequest(pendingNowsRequestedCommand.getCreateNowsRequest(), pendingNowsRequestedCommand.getTargets()));
    }

    @Handles("hearing.command.apply-enforcement-acknowledgement")
    public void applyEnforcementAcknowledgement(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.apply-enforcement-acknowledgement event received {}", envelope.toObfuscatedDebugString());
        }

        final EnforceFinancialImpositionAcknowledgement command = convertToObject(envelope, EnforceFinancialImpositionAcknowledgement.class);

        final UUID requestId = command.getRequestId();

        final Optional<SystemIdMapping> mapping = getSystemIdMappingForRequestId(requestId);

        if (mapping.isPresent()) {

            final UUID hearingId = mapping.get().getTargetId();

            aggregate(HearingAggregate.class, hearingId, envelope,
                    aggregate -> aggregate.applyAccountNumber(command.getRequestId(), command.getAcknowledgement().getAccountNumber()));
        }
    }

    @Handles("hearing.command.enforcement-acknowledgement-error")
    public void enforcementAcknowledgementError(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.enforcement-acknowledgement-error event received {}", envelope.toObfuscatedDebugString());
        }

        final EnforceFinancialImpositionAcknowledgement command = convertToObject(envelope, EnforceFinancialImpositionAcknowledgement.class);

        final UUID requestId = command.getRequestId();

        final Optional<SystemIdMapping> mapping = getSystemIdMappingForRequestId(requestId);

        if (mapping.isPresent()) {

            final UUID hearingId = mapping.get().getTargetId();

            aggregate(HearingAggregate.class, hearingId, envelope,
                    aggregate -> aggregate.recordEnforcementError(command.getRequestId(), command.getAcknowledgement().getErrorCode(), command.getAcknowledgement().getErrorMessage()));
        }
    }

    private Optional<SystemIdMapping> getSystemIdMappingForRequestId(final UUID requestId) {

        final String HEARING_ID = "cpp.hearing.hearingId";

        final String REQUEST_ID = "cpp.hearing.requestId";

        return systemUserProvider
                .getContextSystemUserId()
                .flatMap(uuid -> systemIdMapperClient.findBy(requestId.toString(), REQUEST_ID, HEARING_ID, uuid));
    }
}