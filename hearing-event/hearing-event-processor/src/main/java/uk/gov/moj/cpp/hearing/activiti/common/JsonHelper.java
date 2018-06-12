package uk.gov.moj.cpp.hearing.activiti.common;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.justice.services.messaging.Metadata;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.USER_ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

public class JsonHelper {

    public static final String PROCESS_ID = "processId";
    public static final String CONTEXT = "context";

    private JsonHelper() {
    }


    public static Metadata createMetadataWithProcessIdAndUserId(final String id, final String name, final String processId, final String userId) {
        return metadataFrom(Json.createObjectBuilder()
                .add(ID, id)
                .add(NAME, name)
                .add(PROCESS_ID, processId)
                .add(CONTEXT, Json.createObjectBuilder()
                        .add(USER_ID, userId))
                .build());
    }

    public static JsonEnvelope assembleEnvelopeWithPayloadAndMetaDetails(final JsonObject payload, final String contentType, final String processId, final String userId) {
        final Metadata metadata = createMetadataWithProcessIdAndUserId(UUID.randomUUID().toString(), contentType, processId, userId);
        final JsonObject payloadWithMetada = addMetadataToPayload(payload, metadata);
        return envelopeFrom(metadata, payloadWithMetada);
    }


    private static JsonObject addMetadataToPayload(final JsonObject load, final Metadata metadata) {
        final JsonObjectBuilder job = Json.createObjectBuilder();
        load.entrySet().forEach(entry -> job.add(entry.getKey(), entry.getValue()));
        job.add(JsonEnvelope.METADATA, metadata.asJsonObject());
        return job.build();
    }

    public static Optional<String> getProcessIdFromJsonMetadata(JsonObject jsonMetadata) {
        return JsonObjects.getString(jsonMetadata, new String[]{PROCESS_ID});
    }





}
