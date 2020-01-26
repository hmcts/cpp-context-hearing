package uk.gov.moj.cpp.hearing.command.handler.service;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonObject;

import com.google.common.annotations.VisibleForTesting;

public class ReferenceDataService {

    private static final String REFERENCEDATA_QUERY_COURT_CENTRES = "referencedata.query.courtrooms";

    @Inject
    private Enveloper enveloper;

    @Inject
    @ServiceComponent(COMMAND_API)
    private Requester requester;

    public List<UUID> getAllCrownCourtCentres(final JsonEnvelope inputEnvelope) {

        final JsonObject payload = createObjectBuilder()
                .add("oucodeL1Code", "C")
                .build();
        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(inputEnvelope, REFERENCEDATA_QUERY_COURT_CENTRES).apply(payload);
        return requester.requestAsAdmin(requestEnvelope).payloadAsJsonObject()
                .getJsonArray("organisationunits")
                .getValuesAs(JsonObject.class)
                .stream()
                .map(c -> UUID.fromString(c.getString("id")))
                .collect(Collectors.toList());
    }

    @VisibleForTesting
    void setEnveloper(final Enveloper enveloper) {
        this.enveloper = enveloper;
    }
}
