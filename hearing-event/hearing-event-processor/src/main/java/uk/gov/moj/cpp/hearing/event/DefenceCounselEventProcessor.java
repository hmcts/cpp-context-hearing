package uk.gov.moj.cpp.hearing.event;

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
public class DefenceCounselEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefenceCounselEventProcessor.class);

    private static final String EVENT_RECEIVED_LOG_TEMPLATE = "{} event received {}";

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Handles("hearing.defence-counsel-added")
    public void publishPublicDefenceCounselAddedEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(EVENT_RECEIVED_LOG_TEMPLATE, "hearing.defence-counsel-added", event.toObfuscatedDebugString());
        }

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.defence-counsel-added").apply(event.payloadAsJsonObject()));
    }
}
