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

/**
 * Publishes the public counterpart of each PTPH detail domain event.
 *
 * <p>The commands return {@code 202 Accepted} before the aggregate runs, so these events are
 * how a caller learns that a save, finalise or delete actually took effect.
 *
 * <p>Payloads are passed through unchanged: {@code saved} carries tier, list type and key
 * reason, {@code finalised} and {@code deleted} carry only the hearing id. A consumer needing
 * the values on finalisation queries {@code GET /hearings/{hearingId}/ptph-detail}.
 */
@ServiceComponent(EVENT_PROCESSOR)
public class PtphDetailEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PtphDetailEventProcessor.class);

    private static final String EVENT_RECEIVED_LOG_TEMPLATE = "{} event received {}";

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Handles("hearing.ptph-detail-saved")
    public void publishPublicPtphDetailSavedEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(EVENT_RECEIVED_LOG_TEMPLATE, "hearing.ptph-detail-saved", event.toObfuscatedDebugString());
        }

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.ptph-detail-saved").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.ptph-detail-finalised")
    public void publishPublicPtphDetailFinalisedEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(EVENT_RECEIVED_LOG_TEMPLATE, "hearing.ptph-detail-finalised", event.toObfuscatedDebugString());
        }

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.ptph-detail-finalised").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.ptph-detail-deleted")
    public void publishPublicPtphDetailDeletedEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(EVENT_RECEIVED_LOG_TEMPLATE, "hearing.ptph-detail-deleted", event.toObfuscatedDebugString());
        }

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.ptph-detail-deleted").apply(event.payloadAsJsonObject()));
    }
}
