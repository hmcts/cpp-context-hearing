package uk.gov.moj.cpp.hearing.event.service;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;

import javax.inject.Inject;
import javax.json.JsonObject;

public class ReferenceDataLoader {

    private static final String REFERENCEDATA_QUERY_COURT_CENTRES = "referencedata.query.courtrooms";

    @Inject
    @ServiceComponent(COMMAND_API)
    private Requester requester;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private StringToJsonObjectConverter stringToJsonObjectConverter;

    public List<JsonObject> getAllCrownCourtCentres() {
        final JsonObject payload = createObjectBuilder()
                .add("oucodeL1Code", "C")
                .build();

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(REFERENCEDATA_QUERY_COURT_CENTRES)
                        .withId(randomUUID())
                        .build(),
                payload);

        return requester.requestAsAdmin(requestEnvelope).payloadAsJsonObject()
                .getJsonArray("organisationunits")
                .getValuesAs(JsonObject.class);
    }
}
