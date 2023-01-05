package uk.gov.moj.cpp.hearing.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.HEARING_ID;

@ServiceComponent(EVENT_PROCESSOR)
public class DefendantLegalAidStatusUpdatedProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefendantLegalAidStatusUpdatedProcessor.class);
    private final Enveloper enveloper;
    private final Sender sender;
    private static final String LEGAL_AID_STATUS = "legalAidStatus";
    private static final String DEFENDANT_ID = "defendantId";
    private static final String COMMAND_UPDATE_DEFENDANT_LEGALAID_STATUS_FOR_HEARING = "hearing.command.update-defendant-legalaid-status-for-hearing";


    @Inject
    public DefendantLegalAidStatusUpdatedProcessor(final Enveloper enveloper, final Sender sender) {
        this.enveloper = enveloper;
        this.sender = sender;
    }

    @Handles("public.progression.defendant-legalaid-status-updated")
    public void defendantLegalStatusUpdate(final JsonEnvelope envelop) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("progression.defendant-legalaid-status-updated event received {}", envelop.toObfuscatedDebugString());
        }

        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("INV: will cause hearing.defendant-legalaid-status-updated-for-hearing clienCorrelationId: {}", envelop.metadata().clientCorrelationId().orElse(null));
        }

        this.sender.send(this.enveloper
                .withMetadataFrom(envelop, "hearing.command.update-defendant-legalaid-status")
                .apply(envelop.payloadAsJsonObject()));
    }

    @Handles("hearing.defendant-legalaid-status-updated")
    public void handleDefendantLegalStatusUpdateForHearings(final JsonEnvelope envelop) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("hearing.defendant-legalaid-status-updated event received {}", envelop.toObfuscatedDebugString());
        }
        final JsonObject eventPayload = envelop.payloadAsJsonObject();
        final JsonArray hearingIds = eventPayload.getJsonArray("hearingIds");
        hearingIds.stream().forEach(hearingId -> {
            final JsonObject commandPayload = Json.createObjectBuilder()
                    .add(HEARING_ID, hearingId)
                    .add(DEFENDANT_ID, eventPayload.getString(DEFENDANT_ID))
                    .add(LEGAL_AID_STATUS, eventPayload.getString(LEGAL_AID_STATUS))
                    .build();
            this.sender.send(this.enveloper.withMetadataFrom(envelop, COMMAND_UPDATE_DEFENDANT_LEGALAID_STATUS_FOR_HEARING).apply(commandPayload));
        });
    }

}
