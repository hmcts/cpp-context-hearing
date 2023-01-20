package uk.gov.moj.cpp.hearing.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.HEARING_ID;

import java.util.UUID;

@ServiceComponent(EVENT_PROCESSOR)
public class DefendantLegalAidStatusUpdatedProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefendantLegalAidStatusUpdatedProcessor.class);
    private final Enveloper enveloper;
    private final Sender sender;
    private static final String LEGAL_AID_STATUS = "legalAidStatus";
    private static final String DEFENDANT_ID = "defendantId";
    private static final String COMMAND_UPDATE_DEFENDANT_LEGALAID_STATUS_FOR_HEARING = "hearing.command.update-defendant-legalaid-status-for-hearing";

    @Inject
    private DefendantRepository defendantRepository;

    @Inject
    public DefendantLegalAidStatusUpdatedProcessor(final Enveloper enveloper, final Sender sender, final DefendantRepository defendantRepository) {
        this.enveloper = enveloper;
        this.sender = sender;
        this.defendantRepository = defendantRepository;
    }

    @Handles("public.progression.defendant-legalaid-status-updated")
    public void defendantLegalStatusUpdate(final JsonEnvelope envelop) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("progression.defendant-legalaid-status-updated event received {}", envelop.toObfuscatedDebugString());
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
            final String defendantId = eventPayload.getString(DEFENDANT_ID);
            final Defendant defendant = defendantRepository.findBy(new HearingSnapshotKey(UUID.fromString(defendantId), UUID.fromString(((JsonString)hearingId).getString())));
            if(defendant != null){
                final JsonObject commandPayload = Json.createObjectBuilder()
                        .add(HEARING_ID, hearingId)
                        .add(DEFENDANT_ID, defendantId)
                        .add(LEGAL_AID_STATUS, eventPayload.getString(LEGAL_AID_STATUS))
                        .build();
                this.sender.send(this.enveloper.withMetadataFrom(envelop, COMMAND_UPDATE_DEFENDANT_LEGALAID_STATUS_FOR_HEARING).apply(commandPayload));
            }
        });
    }

}
