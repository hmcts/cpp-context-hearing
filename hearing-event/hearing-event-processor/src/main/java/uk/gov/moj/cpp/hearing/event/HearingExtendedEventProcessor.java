package uk.gov.moj.cpp.hearing.event;

import org.slf4j.Logger;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@ServiceComponent(EVENT_PROCESSOR)
public class HearingExtendedEventProcessor {

    private static final Logger LOGGER = getLogger(HearingExtendedEventProcessor.class);

    @Inject
    private Enveloper enveloper;
    @Inject
    private Sender sender;

    @Handles("public.progression.events.hearing-extended")
    public void onPublicProgressionEventsHearingExtended(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("public.progression.events.hearing-extended event received {}", event.toObfuscatedDebugString());
        }
        sender.send(enveloper.withMetadataFrom(event, "hearing.command.extend-hearing").apply(event.payloadAsJsonObject()));
    }

}
