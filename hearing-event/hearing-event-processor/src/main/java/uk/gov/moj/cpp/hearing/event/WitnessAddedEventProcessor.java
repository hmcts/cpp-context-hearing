package uk.gov.moj.cpp.hearing.event;

import static java.util.UUID.fromString;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class WitnessAddedEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(WitnessAddedEventProcessor.class.getName());

    private static final String ID = "id";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_CLASSIFICATION = "classification";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_FIRST_NAME = "firstName";
    private static final String FIELD_LAST_NAME = "lastName";

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Handles("hearing.events.witness-added")
    public void publishWitnessAddedPublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.witness-added event received {}", event.toObfuscatedDebugString());
        }
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID witnessId = fromString(payload.getString(ID));
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String type = payload.getString(FIELD_TYPE);
        final String classification = payload.getString(FIELD_CLASSIFICATION);
        final String title = payload.containsKey(FIELD_TITLE) ? payload.getString(FIELD_TITLE) : "";
        final String firstName = payload.getString(FIELD_FIRST_NAME);
        final String lastName = payload.getString(FIELD_LAST_NAME);
        final JsonArray defendantIds = payload.getJsonArray("defendantIds");

        defendantIds.forEach(
                defendantId ->
                        this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.defence-witness-added").apply(createObjectBuilder()
                                .add("defendantId", ((JsonString) defendantId).getString())
                                .add("witnessId", witnessId.toString())
                                .add(FIELD_HEARING_ID, hearingId.toString())
                                .add(FIELD_TYPE, type)
                                .add(FIELD_CLASSIFICATION, classification)
                                .add(FIELD_TITLE, title)
                                .add(FIELD_FIRST_NAME, firstName)
                                .add(FIELD_LAST_NAME, lastName)
                                .build()))

        );


        this.sender.send(this.enveloper
                .withMetadataFrom(event, "public.hearing.events.witness-added-updated")
                .apply(createObjectBuilder().add("witnessId", witnessId.toString()).build()));
    }

}
