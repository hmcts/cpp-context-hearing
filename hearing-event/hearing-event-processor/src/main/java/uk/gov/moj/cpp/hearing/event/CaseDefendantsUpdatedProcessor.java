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
public class CaseDefendantsUpdatedProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseDefendantsUpdatedProcessor.class);
    public static final String PROSECUTION_CASE = "prosecutionCase";
    private final Enveloper enveloper;
    private final Sender sender;
    private static final String COMMAND_CASE_DEFENDANTS_UPDATED_FOR_HEARING= "hearing.command.update-case-defendants-for-hearing";


    @Inject
    public CaseDefendantsUpdatedProcessor(final Enveloper enveloper, final Sender sender) {
        this.enveloper = enveloper;
        this.sender = sender;
    }

    @Handles("hearing.case-defendants-updated")
    public void handleCaseDefendantsUpdateForHearing(final JsonEnvelope envelop) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.case-defendants-updated event received {}", envelop.toObfuscatedDebugString());
        }

        final JsonObject eventPayload = envelop.payloadAsJsonObject();
        final JsonArray hearingIds = eventPayload.getJsonArray("hearingIds");
        hearingIds.stream().forEach(hearingId -> {
            final JsonObject commandPayload = Json.createObjectBuilder()
                    .add(HEARING_ID, hearingId)
                    .add(PROSECUTION_CASE, eventPayload.getJsonObject(PROSECUTION_CASE))
                    .build();
            this.sender.send(this.enveloper.withMetadataFrom(envelop, COMMAND_CASE_DEFENDANTS_UPDATED_FOR_HEARING).apply(commandPayload));
        });
    }
}
