package uk.gov.moj.cpp.hearing.command.api.service;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class ReferenceDataService {

    private static final String RESULT_DEFINITIONS = "resultDefinitions";
    private static final String RESULT_QUERY = "referencedata.query-result-definitions";

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    @ServiceComponent(COMMAND_API)
    private Requester requester;

    public ResultDefinition getResults(final Envelope<?> envelope, final String shortCode) {

        final JsonObject getResultDefinitions = createObjectBuilder().add("shortCode", shortCode).build();
        final Envelope<JsonObject> requestEnvelope = Enveloper.envelop(getResultDefinitions)
                .withName(RESULT_QUERY).withMetadataFrom(envelope);
        final Envelope<JsonObject> response = requester.requestAsAdmin(requestEnvelope, JsonObject.class);
        final JsonArray resultDefinitions = response.payload().getJsonArray(RESULT_DEFINITIONS);
        return resultDefinitions.isEmpty() ? ResultDefinition.resultDefinition() : jsonObjectToObjectConverter.convert(resultDefinitions.getJsonObject(0), ResultDefinition.class);
    }

}
