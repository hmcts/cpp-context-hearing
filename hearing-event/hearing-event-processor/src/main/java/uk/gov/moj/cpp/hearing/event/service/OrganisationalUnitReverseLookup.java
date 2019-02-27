package uk.gov.moj.cpp.hearing.event.service;

import static javax.json.Json.createObjectBuilder;

import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.hearing.courts.referencedata.OrganisationunitsResult;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;

@SuppressWarnings({"squid:S00112", "squid:S1181"})
public class OrganisationalUnitReverseLookup {

    public static final String GET_ORGANISATION_UNITS = "referencedata.query.organisationunits";


    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private OrganisationunitsResult organisationUnits(JsonEnvelope context) {
        JsonEnvelope requestEnvelope;
        JsonEnvelope jsonResultEnvelope;
        requestEnvelope = enveloper.withMetadataFrom(context, GET_ORGANISATION_UNITS)
                .apply(createObjectBuilder().build());
        jsonResultEnvelope = requester.request(requestEnvelope);

        final JsonObject organisationalUnitJson = jsonResultEnvelope.payloadAsJsonObject();
        return jsonObjectToObjectConverter.convert(organisationalUnitJson, OrganisationunitsResult.class);
    }

    public OrganisationalUnit getOrganisationUnitByOucodeL3Name(JsonEnvelope context, String oucodeL3Name) {

        oucodeL3Name = normalize(oucodeL3Name);
        try {
            final OrganisationunitsResult organisationunitsResult = organisationUnits(context);
            for (final OrganisationalUnit organisationunits : organisationunitsResult.getOrganisationunits()) {
                if (oucodeL3Name.equals(normalize(organisationunits.getOucodeL2Name()))) {
                    return organisationunits;
                }
            }
        } catch (Throwable tw) {
            throw new RuntimeException(String.format("failed to find organisational unit with oucodeL3Name %s", oucodeL3Name), tw);
        }
        return null;
    }

    private String normalize(final String str) {
        return str.replaceAll(" ", "").toLowerCase();
    }

}
