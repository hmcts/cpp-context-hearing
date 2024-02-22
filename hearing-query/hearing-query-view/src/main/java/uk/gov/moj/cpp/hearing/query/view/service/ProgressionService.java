package uk.gov.moj.cpp.hearing.query.view.service;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.UUID;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

@ApplicationScoped
public class ProgressionService {

    private static final String PROGRESSION_QUERY_APPLICATIONS = "progression.query.application.aaag";

    @Inject
    @ServiceComponent(QUERY_API)
    private Requester requester;

    public Optional<JsonObject> getApplication(final JsonEnvelope envelope, final String applicationId) {
        final JsonObject requestParameter = createObjectBuilder()
                .add("applicationId", applicationId)
                .build();
        final Envelope<JsonObject> requestEnvelop = envelop(requestParameter)
                .withName(PROGRESSION_QUERY_APPLICATIONS)
                .withMetadataFrom(envelope);
        final Envelope<JsonObject> jsonObjectEnvelope = requester.request(requestEnvelop, JsonObject.class);
        return Optional.of(jsonObjectEnvelope.payload());
    }

    public JsonObject retrieveApplication(final JsonEnvelope event, final UUID applicationId) {
        final Optional<JsonObject> applicationPayload = getApplication(event, applicationId.toString());
        if (applicationPayload.isPresent()) {
            return applicationPayload.get();
        }
        throw new IllegalStateException("Application not found for applicationId:" + applicationId);
    }
}
