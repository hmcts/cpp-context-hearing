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
public class DefenceCounselChangeEventProcessor {

    private static final Logger LOGGER = getLogger(DefenceCounselChangeEventProcessor.class);

    @Inject
    private Sender sender;

    @Handles("hearing.defence-counsel-change-ignored")
    public void publishPublicDefenceCounselChangeIgnoredEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.defence-counsel-change-ignored event received {}", event.toObfuscatedDebugString());
        }

        sender.send(envelop(event.payloadAsJsonObject())
                .withName("public.hearing.defence-counsel-change-ignored")
                .withMetadataFrom(event));
    }

    @Handles("hearing.defence-counsel-updated")
    public void publishPublicDefenceCounselUpdatedEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.defence-counsel-updated event received {}", event.toObfuscatedDebugString());
        }

        sender.send(envelop(event.payloadAsJsonObject())
                .withName("public.hearing.defence-counsel-updated")
                .withMetadataFrom(event));
    }

    @Handles("hearing.defence-counsel-removed")
    public void publishPublicDefenceCounselRemovedEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.defence-counsel-removed event received {}", event.toObfuscatedDebugString());
        }

        sender.send(envelop(event.payloadAsJsonObject())
                .withName("public.hearing.defence-counsel-removed")
                .withMetadataFrom(event));
    }

}
