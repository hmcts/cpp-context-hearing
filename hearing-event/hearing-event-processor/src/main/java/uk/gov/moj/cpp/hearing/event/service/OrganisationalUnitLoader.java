package uk.gov.moj.cpp.hearing.event.service;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

public class OrganisationalUnitLoader {

    public static final String GET_ORGANISATION_UNITS_V2 = "referencedata.query.organisation-unit.v2";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    public OrganisationalUnit getOrganisationUnitById(JsonEnvelope context, final UUID organisationUnitId) {
        final JsonObject payload = createObjectBuilder()
                .add("id", organisationUnitId.toString())
                .build();

        final Envelope<JsonObject> requestEnvelope = envelop(payload)
                .withName(GET_ORGANISATION_UNITS_V2)
                .withMetadataFrom(context);

        return requester.request(requestEnvelope, OrganisationalUnit.class).payload();
    }
}
