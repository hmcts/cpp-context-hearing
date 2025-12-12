package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import javax.inject.Inject;
import javax.json.JsonObject;

@ServiceComponent(EVENT_PROCESSOR)
public class CreateProsecutionCaseEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateProsecutionCaseEventProcessor.class);

    @Inject
    private Sender sender;

    @Handles("hearing.events.registered-hearing-against-case")
    public void handleHearingAgainstCaseRegisteredEvent(final JsonEnvelope event) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.registered-hearing-against-case event received {}", event.toObfuscatedDebugString());
        }

        final JsonObject payload = event.payloadAsJsonObject();

        sender.send(
                envelop(
                        createObjectBuilder()
                                .add("prosecutionCaseId", payload.getString("caseId"))
                                .build()
                )
                        .withName("public.events.hearing.prosecution-case-created-in-hearing")
                        .withMetadataFrom(event));
    }

}
