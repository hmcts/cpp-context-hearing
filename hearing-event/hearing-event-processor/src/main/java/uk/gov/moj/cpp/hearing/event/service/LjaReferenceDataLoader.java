package uk.gov.moj.cpp.hearing.event.service;

import static java.util.Objects.isNull;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.hearing.courts.referencedata.EnforcementArea;
import uk.gov.justice.hearing.courts.referencedata.EnforcementAreaBacs;
import uk.gov.justice.hearing.courts.referencedata.LocalJusticeAreas;
import uk.gov.justice.hearing.courts.referencedata.LocalJusticeAreasResult;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.exception.BacsNotFoundException;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

public class LjaReferenceDataLoader {

    public static final String GET_ORGANISATION_UNIT_BY_ID_ID = "referencedata.query.organisation-unit.v2";
    public static final String ENFORCEMENT_AREA_QUERY_NAME = "referencedata.query.enforcement-area";
    public static final String COURT_CODE_QUERY_PARAMETER = "localJusticeAreaNationalCourtCode";
    public static final String COURT_CENTRE_ID_PATH_PARAM = "id";
    public static final String REFERENCEDATA_QUERY_LOCAL_JUSTICE_AREAS = "referencedata.query.local-justice-areas";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private OrganisationalUnit organisationalUnitById(final JsonEnvelope context, final UUID id) {
        final JsonObject payload = createObjectBuilder()
                .add(COURT_CENTRE_ID_PATH_PARAM, id.toString())
                .build();
        final Envelope<JsonObject> requestEnvelope = envelop(payload)
                .withName(GET_ORGANISATION_UNIT_BY_ID_ID)
                .withMetadataFrom(context);
        final JsonEnvelope jsonResultEnvelope = requester.request(requestEnvelope);

        final JsonObject organisationalUnitJson = jsonResultEnvelope.payloadAsJsonObject();
        return jsonObjectToObjectConverter.convert(organisationalUnitJson, OrganisationalUnit.class);

    }

    private EnforcementArea enforcementAreaByLjaCode(final JsonEnvelope context, final String ljaCode) {
        final Envelope<JsonObject> request = envelop(createObjectBuilder().add(COURT_CODE_QUERY_PARAMETER, ljaCode)
                .build())
                .withName(ENFORCEMENT_AREA_QUERY_NAME)
                .withMetadataFrom(context);

        final JsonEnvelope jsonResultEnvelope = requester.requestAsAdmin(envelopeFrom(request.metadata(), request.payload()));
        return jsonObjectToObjectConverter.convert(jsonResultEnvelope.payloadAsJsonObject(), EnforcementArea.class);
    }

    public LocalJusticeAreas getLJAByNationalCourtCode(JsonEnvelope context, final String nationalCourtCode) {

        final JsonObject queryParams = createObjectBuilder().add("nationalCourtCode", nationalCourtCode).build();

        final Envelope<JsonObject> requestEnvelope = envelop(queryParams)
                .withName(REFERENCEDATA_QUERY_LOCAL_JUSTICE_AREAS)
                .withMetadataFrom(context);

        final JsonObject responseJsonObject = requester.requestAsAdmin(envelopeFrom(requestEnvelope.metadata(), requestEnvelope.payload())).payloadAsJsonObject();

        final LocalJusticeAreasResult localJusticeAreasResult = jsonObjectToObjectConverter.convert(responseJsonObject, LocalJusticeAreasResult.class);

        final Optional<LocalJusticeAreas> lja = localJusticeAreasResult.getLocalJusticeAreas().stream()
                .filter(localJusticeAreas -> localJusticeAreas.getNationalCourtCode().equalsIgnoreCase(nationalCourtCode))
                .findFirst();
        return lja.orElseGet(() -> null);
    }

    public LjaDetails getLjaDetails(JsonEnvelope context, final UUID courtCentreId) {
        final OrganisationalUnit organisationUnit = organisationalUnitById(context, courtCentreId);
        final EnforcementAreaBacs enforcementAreaBACS = organisationUnit.getEnforcementArea();
        if (isNull(enforcementAreaBACS)) {
            throw new BacsNotFoundException(String.format("No BACS details found for court centreId %s", courtCentreId));
        }
        final EnforcementArea courtEnforcementArea = enforcementAreaByLjaCode(context, organisationUnit.getLja());
        return LjaDetails.ljaDetails()
                .withLjaCode(courtEnforcementArea.getLocalJusticeArea().getNationalCourtCode())
                .withLjaName(courtEnforcementArea.getLocalJusticeArea().getName())
                .withWelshLjaName(courtEnforcementArea.getLocalJusticeArea().getWelshName())
                .build();
    }
}
