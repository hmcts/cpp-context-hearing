package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class CourtApplicationDeletedProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CourtApplicationDeletedProcessor.class);

    @Inject
    private Sender sender;

    @Handles("public.progression.events.court-application-deleted")
    public void handleCourtApplicationDeletedPublicEvent(final JsonEnvelope envelope) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received '{}' event with payload {}", "public.progression.events.court-application-deleted", envelope.toObfuscatedDebugString());
        }

        final JsonObjectBuilder commandBuilder = Json.createObjectBuilder();
        final JsonObject payload = envelope.payloadAsJsonObject();
        commandBuilder
                .add("hearingId", payload.getJsonString("hearingId"))
                .add("applicationId", payload.getJsonString("applicationId"));

        sender.send(envelopeFrom(metadataFrom(envelope.metadata()).withName("hearing.command.delete-court-application-hearing"),
                commandBuilder.build()));

    }

}
