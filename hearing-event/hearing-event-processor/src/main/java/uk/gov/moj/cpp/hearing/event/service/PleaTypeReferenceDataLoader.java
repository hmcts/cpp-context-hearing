package uk.gov.moj.cpp.hearing.event.service;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.MetadataBuilder;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

public class PleaTypeReferenceDataLoader {
    private static final String REFERENCEDATA_QUERY_PLEA_TYPES = "referencedata.query.plea-types";
    private static final String FIELD_PLEA_STATUS_TYPES = "pleaStatusTypes";
    private static final String FIELD_PLEA_TYPE_GUILTY_FLAG = "pleaTypeGuiltyFlag";
    private static final String GUILTY_FLAG_YES = "Yes";
    private static final String FIELD_PLEA_VALUE = "pleaValue";

    @Inject
    @ServiceComponent(COMMAND_API)
    private Requester requester;

    public Set<String> retrieveGuiltyPleaTypes() {
        final MetadataBuilder metadataBuilder = metadataBuilder()
                .withId(randomUUID())
                .withName(REFERENCEDATA_QUERY_PLEA_TYPES);

        final Envelope<JsonObject> pleaTypes = requester.requestAsAdmin(envelopeFrom(metadataBuilder, createObjectBuilder()), JsonObject.class);
        final JsonArray pleaStatusTypes = pleaTypes.payload().getJsonArray(FIELD_PLEA_STATUS_TYPES);

        return pleaStatusTypes.stream()
                .filter(jsonValue -> isGuiltyPleaType((JsonObject) jsonValue))
                .map(jsonValue -> ((JsonObject)jsonValue).getString(FIELD_PLEA_VALUE))
                .collect(Collectors.toSet());
    }

    private boolean isGuiltyPleaType(JsonObject jsonValue) {
        return GUILTY_FLAG_YES.equalsIgnoreCase(jsonValue.getString(FIELD_PLEA_TYPE_GUILTY_FLAG));
    }
}
