package uk.gov.moj.cpp.hearing.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@ServiceComponent(EVENT_PROCESSOR)
public class YouthCourtDefendantsUpdatedProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(YouthCourtDefendantsUpdatedProcessor.class);
    private final Enveloper enveloper;
    private final Sender sender;


    @Inject
    public YouthCourtDefendantsUpdatedProcessor(final Enveloper enveloper, final Sender sender) {
        this.enveloper = enveloper;
        this.sender = sender;
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    @Handles("hearing.event.defendants-in-youthcourt-updated")
    public void processYouthCourtDefendantsUpdated(final JsonEnvelope envelop) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.event.defendants-in-youthcourt-updated event received {}", envelop.toObfuscatedDebugString());
        }
        this.sender.send(this.enveloper.withMetadataFrom(envelop, "public.hearing.defendants-in-youthcourt-updated")
                .apply(envelop.payloadAsJsonObject()));
    }


}