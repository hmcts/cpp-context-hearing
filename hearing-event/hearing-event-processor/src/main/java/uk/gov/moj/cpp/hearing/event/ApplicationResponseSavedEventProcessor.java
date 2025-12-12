package uk.gov.moj.cpp.hearing.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@ServiceComponent(EVENT_PROCESSOR)
public class ApplicationResponseSavedEventProcessor {

    public static final String PUBLIC_HEARING_APPLICATION_RESPONSE_SAVED = "public.hearing.application-response-saved";
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationResponseSavedEventProcessor.class);
    private final Enveloper enveloper;
    private final Sender sender;

    @Inject
    public ApplicationResponseSavedEventProcessor(final Enveloper enveloper, final Sender sender) {
        this.enveloper = enveloper;
        this.sender = sender;
    }

    @Handles("hearing.application-response-saved")
    public void publicApplicationResponseSavedPublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.application-response-saved event received {}", event.toObfuscatedDebugString());
        }
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_APPLICATION_RESPONSE_SAVED).apply(event.payloadAsJsonObject()));
    }

}
