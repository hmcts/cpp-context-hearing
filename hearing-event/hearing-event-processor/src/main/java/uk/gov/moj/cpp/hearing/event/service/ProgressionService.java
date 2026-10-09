package uk.gov.moj.cpp.hearing.event.service;

import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
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
    public static final String PROSECUTION_CASE_DETAILS = "progression.query.prosecutioncase-details";
    private static final String PROSECUTION_CASE = "prosecutionCase";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    public Optional<JsonObject> getApplicationDetails(final JsonEnvelope jsonEnvelope, UUID applicationId) {

        final JsonObject payload = createObjectBuilder()
                .add("applicationId", applicationId.toString())
                .build();

        final Envelope<JsonObject> requestEnvelope = envelop(payload)
                .withName(SEARCH_APPLICATION)
                .withMetadataFrom(jsonEnvelope);

        return Optional.ofNullable(requester.request(requestEnvelope, JsonObject.class).payload());
    }

    /**
     * Returns the current state of the prosecution case held by progression.
     */
    public Optional<ProsecutionCase> getProsecutionCaseDetails(final JsonEnvelope jsonEnvelope, final UUID caseId) {

        final JsonObject payload = createObjectBuilder()
                .add("caseId", caseId.toString())
                .build();

        final Envelope<JsonObject> requestEnvelope = envelop(payload)
                .withName(PROSECUTION_CASE_DETAILS)
                .withMetadataFrom(jsonEnvelope);

        return Optional.ofNullable(requester.requestAsAdmin(requestEnvelope, JsonObject.class).payload())
                .filter(response -> response.containsKey(PROSECUTION_CASE))
                .map(response -> jsonObjectToObjectConverter.convert(response.getJsonObject(PROSECUTION_CASE), ProsecutionCase.class));
    }

}
