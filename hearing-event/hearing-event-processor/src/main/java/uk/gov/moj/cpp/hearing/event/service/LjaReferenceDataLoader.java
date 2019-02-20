package uk.gov.moj.cpp.hearing.event.service;

import static java.util.Objects.isNull;
import static javax.json.Json.createObjectBuilder;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.hearing.courts.referencedata.EnforcementArea;
import uk.gov.justice.hearing.courts.referencedata.EnforcementAreaBacs;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

@SuppressWarnings({"squid:S00112", "squid:S1181"})
public class LjaReferenceDataLoader {

    public static final String GET_ORGANISATION_UNIT_BY_ID_ID = "referencedata.query.organisation-unit.v2";
    public static final String GET_ENFORCEMENT_AREA_BY_COURT_CODE = "referencedata.query.enforcement-area";

    public static final String COURT_CODE_QUERY_PARAMETER = "localJusticeAreaNationalCourtCode";
    public static final String COURT_CENTRE_ID_PATH_PARAM = "id";


    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private Enveloper enveloper;


    private OrganisationalUnit organisationalUnitById(JsonEnvelope context, final UUID id) {
        JsonEnvelope requestEnvelope;
        JsonEnvelope jsonResultEnvelope;
        requestEnvelope = enveloper.withMetadataFrom(context, GET_ORGANISATION_UNIT_BY_ID_ID)
                .apply(createObjectBuilder().add(COURT_CENTRE_ID_PATH_PARAM, id.toString())
                        .build());
        jsonResultEnvelope = requester.request(requestEnvelope);

        final JsonObject organisationalUnitJson = jsonResultEnvelope.payloadAsJsonObject();
        return jsonObjectToObjectConverter.convert(organisationalUnitJson, OrganisationalUnit.class);

    }

    private EnforcementArea enforcementAreaByLjaCode(JsonEnvelope context, final String ljaCode) {
        JsonEnvelope requestEnvelope;
        JsonEnvelope jsonResultEnvelope;
        requestEnvelope = enveloper.withMetadataFrom(context, GET_ENFORCEMENT_AREA_BY_COURT_CODE)
                .apply(createObjectBuilder().add(COURT_CODE_QUERY_PARAMETER, ljaCode)
                        .build());
        jsonResultEnvelope = requester.requestAsAdmin(requestEnvelope);

        return jsonObjectToObjectConverter.convert(jsonResultEnvelope.payloadAsJsonObject(), EnforcementArea.class);
    }

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    public LjaDetails getLjaDetailsByCourtCentreId(JsonEnvelope context, final UUID courtCentreId) {
        OrganisationalUnit organisationUnit = null;

        try {
            organisationUnit = organisationalUnitById(context, courtCentreId);
        } catch (Throwable tw) {
            throw new RuntimeException(String.format("failed to find organisational unit with court centreId %s", courtCentreId), tw);
        }

        final EnforcementArea enforcementArea;

        try {
            enforcementArea = enforcementAreaByLjaCode(context, organisationUnit.getLja());
        } catch (Throwable tw) {
            throw new RuntimeException(String.format("failed to find enforcement  area for lja code %s organisational unit (from  court centreId %s)", organisationUnit.getLja(), courtCentreId), tw);
        }

        final EnforcementAreaBacs enforcementAreaBACS = organisationUnit.getEnforcementArea();

        if (isNull(enforcementAreaBACS)) {
            throw new RuntimeException(String.format("No BACS details found for court centreId %s", courtCentreId));
        }

        return LjaDetails.ljaDetails()
                .withLjaCode(enforcementArea.getLocalJusticeArea().getNationalCourtCode())
                .withEnforcementPhoneNumber(enforcementArea.getPhone())
                .withEnforcementEmail(enforcementArea.getEmail())
                .withLjaName(enforcementArea.getLocalJusticeArea().getName())
                .withAccountDivisionCode(enforcementArea.getAccountDivisionCode() == null ? null : "" + enforcementArea.getAccountDivisionCode())
                .withBacsSortCode(enforcementAreaBACS.getBankAccntSortCode())
                .withBacsBankName(enforcementAreaBACS.getBankAccntName())
                .withBacsAccountNumber(enforcementAreaBACS.getBankAccntNum().toString())
                .withEnforcementAddress(
                        Address.address()
                                .withAddress1(enforcementArea.getAddress1())
                                .withAddress2(enforcementArea.getAddress2())
                                .withAddress3(enforcementArea.getAddress3())
                                .withAddress4(enforcementArea.getAddress4())
                                .withPostcode(enforcementArea.getPostcode())
                                .build()
                )
                .build();
    }


}
