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

    private static final String PROGRESSION_QUERY_APPLICATION_AAAG = "progression.query.application.aaag";
    private static final String PROGRESSION_QUERY_APPLICATION_ONLY = "progression.query.application-only";
    private static final String PROGRESSION_QUERY_APPLICATION_SUMMARY = "progression.query.application.summary";

    private static final String FIELD_APPLICATION_ID = "applicationId";

    @Inject
    @ServiceComponent(QUERY_API)
    private Requester requester;

    public Optional<JsonObject> getApplication(final JsonEnvelope envelope, final String applicationId) {
        final JsonObject requestParameter = createObjectBuilder()
                .add(FIELD_APPLICATION_ID, applicationId)
                .build();
        final Envelope<JsonObject> requestEnvelop = envelop(requestParameter)
                .withName(PROGRESSION_QUERY_APPLICATION_AAAG)
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

    public Optional<JsonObject> getApplicationOnly(final JsonEnvelope envelope, final String applicationId) {
        final JsonObject requestParameter = createObjectBuilder()
                .add(FIELD_APPLICATION_ID, applicationId)
                .build();
        final Envelope<JsonObject> requestEnvelop = envelop(requestParameter)
                .withName(PROGRESSION_QUERY_APPLICATION_ONLY)
                .withMetadataFrom(envelope);
        final Envelope<JsonObject> jsonObjectEnvelope = requester.request(requestEnvelop, JsonObject.class);
        return Optional.of(jsonObjectEnvelope.payload());
    }

    public JsonObject retrieveApplicationOnly(final JsonEnvelope event, final UUID applicationId) {
        final Optional<JsonObject> applicationPayload = getApplicationOnly(event, applicationId.toString());
        if (applicationPayload.isPresent()) {
            return applicationPayload.get();
        }
        throw new IllegalStateException("Application not found for applicationId:" + applicationId);
    }

    public Optional<JsonObject> retrieveApplicationsByParentId(final JsonEnvelope envelope, final String applicationId) {
        final JsonObject requestParameter = createObjectBuilder()
                .add(FIELD_APPLICATION_ID, applicationId.toString())
                .build();
        final Envelope<JsonObject> requestEnvelop = envelop(requestParameter)
                .withName(PROGRESSION_QUERY_APPLICATION_SUMMARY)
                .withMetadataFrom(envelope);
        final Envelope<JsonObject> jsonObjectEnvelope = requester.requestAsAdmin(requestEnvelop, JsonObject.class);
        return Optional.of(jsonObjectEnvelope.payload());
    }

    public JsonObject retrieveApplicationsByParentId(final JsonEnvelope event, final UUID applicationId) {
        final Optional<JsonObject> applicationPayload = retrieveApplicationsByParentId(event, applicationId.toString());
        if (applicationPayload.isPresent()) {
            return applicationPayload.get();
        }
        throw new IllegalStateException("Application not found for parent applicationId:" + applicationId);
    }
}
