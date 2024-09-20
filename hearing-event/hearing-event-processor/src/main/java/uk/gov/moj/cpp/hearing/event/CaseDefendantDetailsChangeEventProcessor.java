package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class CaseDefendantDetailsChangeEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseDefendantDetailsChangeEventProcessor.class);
    @Inject
    private Enveloper enveloper;
    @Inject
    private Sender sender;

    @Handles("public.progression.case-defendant-changed")
    public void processPublicCaseDefendantChanged(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("public.progression.case-defendant-changed event received {}", event.toObfuscatedDebugString());
        }
        //TODO should be removed when GPE-7192 defect fixed
        final JsonObject defendants = createObjectBuilder()
                .add("defendants", createArrayBuilder().add(createObjectBuilder(event.payloadAsJsonObject().getJsonObject("defendant")).build()))
                .build();
        sender.send(enveloper.withMetadataFrom(event, "hearing.update-case-defendant-details").apply(defendants));
    }

    @Handles("hearing.update-case-defendant-details-enriched-with-hearing-ids")
    public void enrichDefendantDetails(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.update-case-defendant-details-enriched-with-hearing-ids event received {}", event.toObfuscatedDebugString());
        }
        sender.send(enveloper.withMetadataFrom(event, "hearing.update-case-defendant-details-against-hearing-aggregate").apply(event.payloadAsJsonObject()));
    }
}