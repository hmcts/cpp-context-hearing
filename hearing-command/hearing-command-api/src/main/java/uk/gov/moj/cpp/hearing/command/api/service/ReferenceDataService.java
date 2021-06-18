package uk.gov.moj.cpp.hearing.command.api.service;

import static java.util.stream.Collectors.toList;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.messaging.Envelope.metadataFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.List;
import java.util.UUID;

public class ReferenceDataService {

    private static final String RESULT_DEFINITIONS = "resultDefinitions";
    private static final String RESULT_QUERY = "referencedata.query-result-definitions";
    private static final String REF_DATA_HEARING_TYPES = "referencedata.query.hearing-types";
    private static final String TRIAL_TYPE_FLAG = "trialTypeFlag";
    private static final String HEARING_TYPES = "hearingTypes";
    private static final String HEARING_TYPE_ID = "id";

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

    public List<UUID> getTrialHearingTypes(final JsonEnvelope event) {

        final JsonObject payload = createObjectBuilder().build();

        final Metadata metadata = metadataFrom(event.metadata())
                .withName(REF_DATA_HEARING_TYPES)
                .build();

        final JsonEnvelope jsonEnvelop = requester.request(envelopeFrom(metadata, payload));
        final JsonArray hearingTypes = jsonEnvelop.payloadAsJsonObject().getJsonArray(HEARING_TYPES);

        return hearingTypes.stream()
                .filter(jsonValue -> {
                    final JsonObject jsonObject = (JsonObject) jsonValue;
                    return Boolean.TRUE.equals(jsonObject.getBoolean(TRIAL_TYPE_FLAG));
                })
                .map(jsonValue -> {
                    final JsonObject jsonObject = (JsonObject) jsonValue;
                    return UUID.fromString(jsonObject.getString(HEARING_TYPE_ID));
                })
                .collect(toList());

    }

}
