package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;


import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class PleaUpdateEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PleaUpdateEventProcessor.class);
    private static final String PLEA_MODEL = "pleaModel";
    private final Enveloper enveloper;
    private final Sender sender;
    private static final String OFFENCE_ID = "offenceId";
    private static final String APPLICATION_ID = "applicationId";

    @Inject
    public PleaUpdateEventProcessor(final Enveloper enveloper, final Sender sender) {
        this.enveloper = enveloper;
        this.sender = sender;
    }

    @Handles("hearing.hearing-offence-plea-updated")
    public void offencePleaUpdate(final JsonEnvelope envelop) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.hearing-offence-plea-updated event received {}", envelop.toObfuscatedDebugString());
        }

        if(isOffenceIdInPayload(envelop)) {
            this.sender.send(Enveloper.envelop(envelop.payloadAsJsonObject()).withName("hearing.command.update-plea-against-offence").withMetadataFrom(envelop));
            this.sender.send(Enveloper.envelop(createObjectBuilder()
                    .add(OFFENCE_ID, getOffenceIdFromPayload(envelop))
                    .build()).withName("public.hearing.plea-updated").withMetadataFrom(envelop));
        }else{
            this.sender.send(Enveloper.envelop(createObjectBuilder()
                    .add(APPLICATION_ID, getApplicationIdFromPayload(envelop))
                    .build()).withName("public.hearing.plea-updated").withMetadataFrom(envelop));
        }
        this.sender.send(Enveloper.envelop(envelop.payloadAsJsonObject()).withName("public.hearing.hearing-offence-plea-updated").withMetadataFrom(envelop));

    }

    @Handles("hearing.events.enrich-update-plea-with-associated-hearings")
    public void enrichedUpdatedPlea(final JsonEnvelope event) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.enrich-update-plea-with-associated-hearings event received {}", event.toObfuscatedDebugString());
        }
        this.sender.send(Enveloper.envelop(event.payloadAsJsonObject())
                .withName("hearing.command.enrich-update-plea-with-associated-hearings").withMetadataFrom(event));
    }

    private boolean isOffenceIdInPayload(JsonEnvelope envelop){
        return envelop.payloadAsJsonObject().getJsonObject(PLEA_MODEL).get(OFFENCE_ID) != null;
    }
    private String getOffenceIdFromPayload(JsonEnvelope envelop) {
        return envelop.payloadAsJsonObject().getJsonObject(PLEA_MODEL).getString(OFFENCE_ID);
    }
    private String getApplicationIdFromPayload(JsonEnvelope envelop) {
        return envelop.payloadAsJsonObject().getJsonObject(PLEA_MODEL).getString(APPLICATION_ID);
    }
}