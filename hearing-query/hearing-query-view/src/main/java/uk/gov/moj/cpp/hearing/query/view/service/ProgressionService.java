package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.model.ApplicationWithStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

@ApplicationScoped
public class ProgressionService {

    private static final String PROGRESSION_QUERY_APPLICATION_AAAG = "progression.query.application.aaag";
    private static final String PROGRESSION_QUERY_APPLICATION_ONLY = "progression.query.application-only";
    static final String PROGRESSION_QUERY_APPLICATION_STATUS = "progression.query.application-status";
    private static final String PROGRESSION_QUERY_APPLICATION_SUMMARY = "progression.query.application.summary";

    private static final String FIELD_APPLICATION_ID = "applicationId";
    private static final String FIELD_APPLICATION_IDS = "applicationIds";
    static final String APPLICATIONS_WITH_STATUS = "applicationsWithStatus";
    static final String APPLICATION_ID = "applicationId";
    static final String APPLICATION_STATUS = "applicationStatus";

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

    public List<ApplicationWithStatus> getApplicationStatus(final List<String> applicationIdList) {
        final String commaSeparatedApplicationIds = String.join(",", applicationIdList);

        final JsonObject requestParameter = createObjectBuilder().add(FIELD_APPLICATION_IDS, commaSeparatedApplicationIds).build();

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(PROGRESSION_QUERY_APPLICATION_STATUS)
                        .withId(randomUUID()).build(),
                requestParameter);

        final Envelope<JsonObject> jsonEnvelope = requester.requestAsAdmin(requestEnvelope, JsonObject.class);

        if (nonNull(jsonEnvelope) && nonNull(jsonEnvelope.payload())
                && jsonEnvelope.payload().containsKey(APPLICATIONS_WITH_STATUS)) {
            final JsonArray jsonArray = jsonEnvelope.payload().getJsonArray(APPLICATIONS_WITH_STATUS);

            return jsonArray.stream()
                    .map(JsonValue::asJsonObject)
                    .map(jobj -> new ApplicationWithStatus(jobj.getString(APPLICATION_ID), jobj.getString(APPLICATION_STATUS)))
                    .toList();
        }

        return emptyList();
    }
}
