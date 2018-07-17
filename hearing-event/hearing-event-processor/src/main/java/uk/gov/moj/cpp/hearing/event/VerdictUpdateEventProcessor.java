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
public class VerdictUpdateEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerdictUpdateEventProcessor.class);

    private final Enveloper enveloper;
    private final Sender sender;

    @Inject
    public VerdictUpdateEventProcessor(final Enveloper enveloper, final Sender sender) {
        this.enveloper = enveloper;
        this.sender = sender;
    }

    @Handles("hearing.offence-verdict-updated")
    public void verdictUpdate(final JsonEnvelope envelop) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.offence-verdict-updated event received {}", envelop.toObfuscatedDebugString());
        }
        this.sender.send(this.enveloper.withMetadataFrom(envelop, "public.hearing.verdict-updated")
                .apply(createObjectBuilder()
                        .add("hearingId", envelop.payloadAsJsonObject().getJsonString("hearingId"))
                        .build()
                )
        );
    }

}



