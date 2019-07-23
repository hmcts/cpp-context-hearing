package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.Objects.nonNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.json.schemas.staging.EnforceFinancialImposition;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.SystemUserProvider;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.moj.cpp.hearing.event.nows.mapper.EnforceFinancialImpositionMapper;
import uk.gov.moj.cpp.hearing.nows.events.PendingNowsRequested;
import uk.gov.moj.cpp.systemidmapper.client.SystemIdMap;
import uk.gov.moj.cpp.systemidmapper.client.SystemIdMapperClient;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1135"})
@ServiceComponent(EVENT_PROCESSOR)
public class StagingEnforcementEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StagingEnforcementEventProcessor.class);
    private static final String ORIGINATOR = "originator";
    private static final String COURTS = "Courts";

    @Inject
    private Sender sender;

    @Inject
    private Enveloper enveloper;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private SystemUserProvider userProvider;

    @Inject
    private SystemIdMapperClient idMapperClient;

    @Handles("hearing.events.pending-nows-requested")
    public void pendingNowsRequested(final JsonEnvelope event) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.pending-nows-requested event received {}", event.toObfuscatedDebugString());
        }

        final PendingNowsRequested pendingNowsRequested = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(),
                PendingNowsRequested.class);

        final CreateNowsRequest nowsRequest = pendingNowsRequested.getCreateNowsRequest();

        final UUID hearingId = pendingNowsRequested.getCreateNowsRequest().getHearing().getId();

        nowsRequest.getNows().stream().filter(now -> nonNull(now.getFinancialOrders())).forEach(now -> {

            final UUID requestId = now.getId();

            addMappingForHearingId(requestId, hearingId);

            final EnforceFinancialImposition enforceFinancialImposition = new EnforceFinancialImpositionMapper(requestId, pendingNowsRequested.getCreateNowsRequest(), pendingNowsRequested.getTargets()).map();

            final JsonObject enforceFinancialImpositionPayload = this.objectToJsonObjectConverter.convert(enforceFinancialImposition);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Enforce Financial Imposition Payload - {}", enforceFinancialImpositionPayload);
            }

            this.sender.send(this.enveloper.withMetadataFrom(event, "stagingenforcement.enforce-financial-imposition").apply(enforceFinancialImpositionPayload));

        });
    }

    @Handles("public.stagingenforcement.enforce-financial-imposition-acknowledgement")
    public void processAcknowledgement(final JsonEnvelope event) {

        //Only deal with hearing messages where originator is courts
        final Optional<String> originator = JsonObjects.getString(event.payloadAsJsonObject(), ORIGINATOR);

        Optional<String> errorCode = Optional.empty();

        final String acknowledgementLbl = "acknowledgement";

        final String errorCodeLbl = "errorCode";

        final Optional<JsonObject> acknowledgement = JsonObjects.getJsonObject(event.payloadAsJsonObject(), acknowledgementLbl);

        if(acknowledgement.isPresent()) {
            errorCode = JsonObjects.getString(acknowledgement.get(), errorCodeLbl);
        }

        if (originator.isPresent() && COURTS.equalsIgnoreCase(originator.get()) && !errorCode.isPresent()) {
            this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.apply-enforcement-acknowledgement").apply(event.payloadAsJsonObject()));
        }

        if (originator.isPresent() && COURTS.equalsIgnoreCase(originator.get()) && errorCode.isPresent()) {
            this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.enforcement-acknowledgement-error").apply(event.payloadAsJsonObject()));
        }

        if(originator.isPresent() && !COURTS.equalsIgnoreCase(originator.get())) {
            LOGGER.warn("Received public.stagingenforcement.enforce-financial-imposition-acknowledgement event for different originator {}", event.payloadAsJsonObject());
        }
    }

    private void addMappingForHearingId(final UUID requestId, final UUID hearingId) {

        final String HEARING_ID = "cpp.hearing.hearingId";

        final String REQUEST_ID = "cpp.hearing.requestId";

        final SystemIdMap systemIdMap = new SystemIdMap(requestId.toString(), REQUEST_ID, hearingId, HEARING_ID);

        final Optional<UUID> contextSystemUserId = userProvider.getContextSystemUserId();

        contextSystemUserId.ifPresent(uuid -> idMapperClient.add(systemIdMap, uuid));
    }
}
