package uk.gov.moj.cpp.hearing.event.service;

import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

public class ProgressionService {

    public static final String SEARCH_APPLICATION = "progression.query.application-only";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    public Optional<JsonObject> getApplicationDetails(final JsonEnvelope jsonEnvelope, UUID applicationId) {

        final JsonObject payload = createObjectBuilder()
                .add("applicationId", applicationId.toString())
                .build();

        final Envelope<JsonObject> requestEnvelope = envelop(payload)
                .withName(SEARCH_APPLICATION)
                .withMetadataFrom(jsonEnvelope);

        return Optional.ofNullable(requester.request(requestEnvelope, JsonObject.class).payload());
    }

}
