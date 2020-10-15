package uk.gov.moj.cpp.hearing.event;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;

@ServiceComponent(EVENT_PROCESSOR)
public class ProsecutionCounselChangeEventProcessor {

    private static final Logger LOGGER = getLogger(ProsecutionCounselChangeEventProcessor.class);

    @Inject
    private Sender sender;

    @Handles("hearing.prosecution-counsel-change-ignored")
    public void publishPublicProsecutionCounselChangeIgnoredEvent(final JsonEnvelope event) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.prosecution-counsel-change-ignored event received {}", event.toObfuscatedDebugString());
        }

        sender.send(envelop(event.payloadAsJsonObject())
                .withName("public.hearing.prosecution-counsel-change-ignored")
                .withMetadataFrom(event));
    }

    @Handles("hearing.prosecution-counsel-updated")
    public void publishPublicProsecutionCounselUpdatedEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.prosecution-counsel-updated event received {}", event.toObfuscatedDebugString());
        }

        sender.send(envelop(event.payloadAsJsonObject())
                .withName("public.hearing.prosecution-counsel-updated")
                .withMetadataFrom(event));
    }

    @Handles("hearing.prosecution-counsel-removed")
    public void publishPublicProsecutionCounselRemovedEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.prosecution-counsel-removed event received {}", event.toObfuscatedDebugString());
        }

        sender.send(envelop(event.payloadAsJsonObject())
                .withName("public.hearing.prosecution-counsel-removed")
                .withMetadataFrom(event));
    }

}
