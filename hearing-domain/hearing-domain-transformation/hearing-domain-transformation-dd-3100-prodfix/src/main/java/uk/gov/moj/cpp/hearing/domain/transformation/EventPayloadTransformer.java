package uk.gov.moj.cpp.hearing.domain.transformation;

import static javax.json.Json.createReader;
import static uk.gov.moj.cpp.hearing.domain.transformation.TargetIds.TARGET_IDS_TO_REPLACE;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.StringReader;

import javax.json.JsonReader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class EventPayloadTransformer {

    private static final String TARGET_ID_ATTRIBUTE = "targetId";
    private static final String TARGET_ATTRIBUTE = "target";

    public javax.json.JsonObject transform(final JsonEnvelope eventEnvelope) {
        final String eventPayload = eventEnvelope.payload().toString();

        final JsonObject payloadToTransform = new Gson().fromJson(eventPayload, JsonObject.class);
        final JsonObject targetAsJsonObject = payloadToTransform.get(TARGET_ATTRIBUTE).getAsJsonObject();
        final String targetIdToReplace = targetAsJsonObject.get(TARGET_ID_ATTRIBUTE).getAsString();

        targetAsJsonObject.addProperty(TARGET_ID_ATTRIBUTE, TARGET_IDS_TO_REPLACE.get(targetIdToReplace));

        final String postTransformedPayload = payloadToTransform.toString();

        try (final JsonReader jsonReader = createReader(new StringReader(postTransformedPayload))) {
            return jsonReader.readObject();
        }
    }

}
