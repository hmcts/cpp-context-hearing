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
public class HearingEventProcessor {

    private final Enveloper enveloper;
    private final Sender sender;

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingEventProcessor.class);

    @Inject
    public HearingEventProcessor(final Enveloper enveloper, final Sender sender) {
        this.enveloper = enveloper;
        this.sender = sender;
    }

    @Handles("hearing.draft-result-saved")
    public void publicDraftResultSavedPublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.draft-result-saved event received {}", event.toObfuscatedDebugString());
        }

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.draft-result-saved").apply(event.payloadAsJsonObject()));
    }

}
