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
    private final Enveloper enveloper;
    private final Sender sender;

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

        this.sender.send(this.enveloper.withMetadataFrom(envelop, "hearing.command.update-plea-against-offence").apply(envelop.payloadAsJsonObject()));

        this.sender.send(this.enveloper.withMetadataFrom(envelop, "public.hearing.plea-updated")
                .apply(createObjectBuilder()
                        .add("offenceId", envelop.payloadAsJsonObject().getJsonObject("plea").getJsonString("offenceId"))
                        .build()));
    }

    @Handles("hearing.events.enrich-update-plea-with-associated-hearings")
    public void enrichedUpdatedPlea(final JsonEnvelope event) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.enrich-update-plea-with-associated-hearings event received {}", event.toObfuscatedDebugString());
        }

        this.sender.send(enveloper.withMetadataFrom(event, "hearing.command.enrich-update-plea-with-associated-hearings").apply(event.payloadAsJsonObject()));
    }
}