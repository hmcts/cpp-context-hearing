package uk.gov.moj.cpp.hearing.event.service;

import static javax.json.Json.createObjectBuilder;

import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.Courtrooms;
import uk.gov.justice.hearing.courts.referencedata.OuCourtRoomsResult;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.inject.Inject;
import javax.json.JsonObject;

@SuppressWarnings({"squid:S00112", "squid:S1181"})
public class CourtHouseReverseLookup {

    public static final String GET_COURT_HOUSES = "referencedata.query.courtrooms";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private OuCourtRoomsResult courtRoomsResult(JsonEnvelope context) {
        JsonEnvelope requestEnvelope;
        JsonEnvelope jsonResultEnvelope;
        requestEnvelope = enveloper.withMetadataFrom(context, GET_COURT_HOUSES)
                .apply(createObjectBuilder().build());
        jsonResultEnvelope = requester.request(requestEnvelope);

        final JsonObject json = jsonResultEnvelope.payloadAsJsonObject();
        return jsonObjectToObjectConverter.convert(json, OuCourtRoomsResult.class);

    }

    public Optional<CourtCentreOrganisationUnit> getCourtCentreByName(final JsonEnvelope context, final String name) {

        final String normalizedName = normalize(name);
        return courtRoomsResult(context).getOrganisationunits().stream()
                .filter(cc -> normalizedName.equals(normalize(cc.getOucodeL3Name())))
                .findFirst();
    }

    //assume room names arnt not globally unique but arr just seached within court house
    public Optional<Courtrooms> getCourtRoomByRoomName(final CourtCentreOrganisationUnit courtcentreOrganisationunits, final String roomName) {
        final String normalizedName = normalize(roomName);
        return courtcentreOrganisationunits.getCourtrooms().stream()
                .filter(cr -> normalizedName.equals(normalize(cr.getCourtroomName())))
                .findFirst();
    }

    private String normalize(final String name) {
        return name.toLowerCase();
    }

}
