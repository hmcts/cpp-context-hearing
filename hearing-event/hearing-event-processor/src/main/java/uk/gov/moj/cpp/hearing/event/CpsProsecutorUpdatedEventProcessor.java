package uk.gov.moj.cpp.hearing.event;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;

@ServiceComponent(EVENT_PROCESSOR)
public class CpsProsecutorUpdatedEventProcessor {

    private static final Logger LOGGER = getLogger(CpsProsecutorUpdatedEventProcessor.class);

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Handles("public.progression.events.cps-prosecutor-updated")
    public void cpsProsecutorUpdated(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("public.progression.events.cps-prosecutor-updated event received {}", event.toObfuscatedDebugString());
        }

        if(event.payloadAsJsonObject().getJsonArray("hearingIds").isEmpty()){
            if(LOGGER.isDebugEnabled()) {
                LOGGER.info("public.progression.events.cps-prosecutor-updated event received without hearings {}", event.toObfuscatedDebugString());
            }
        }else{
            sender.send(Enveloper.envelop(event.payloadAsJsonObject())
                    .withName("hearing.command.update-cps-prosecutor-with-associated-hearings")
                    .withMetadataFrom(event));
        }
    }
}
