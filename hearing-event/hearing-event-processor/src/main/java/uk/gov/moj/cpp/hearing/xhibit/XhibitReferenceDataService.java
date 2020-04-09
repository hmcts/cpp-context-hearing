package uk.gov.moj.cpp.hearing.xhibit;

import static java.lang.String.format;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.xhibit.CourtLocation;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

public class XhibitReferenceDataService {

    private static final String REFERENCEDATA_QUERY_XHIBIT_COURT_MAPPINGS = "referencedata.query.cp-xhibit-court-mappings";
    private static final String REFERENCEDATA_QUERY_COURTROOM = "referencedata.query.courtroom";
    private static final String REFERENCE_DATA_HEARING_TYPES = "referencedata.query.hearing-types";
    private static final String REFERENCEDATA_QUERY_ORGANISATION_UNITS = "referencedata.query.organisationunits";
    private static final int UNMAPPED_COURT_ROOM_NUMBER = -99;

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @SuppressWarnings("squid:S1612")
    public List<String> getCourtCentreIdsForCrestId(final Envelope envelope, final String crownCourtCrestId) {

        final List<CourtLocation> courtLocations = getSitesForCrownCourt(envelope, crownCourtCrestId);

        return courtLocations.stream()
                .map(courtLocation -> getOrganisationUnitId(envelope, courtLocation.getOuCode()))
                .distinct()
                .map(x -> x.toString())
                .collect(Collectors.toList());
    }

    private List<CourtLocation> getSitesForCrownCourt(final Envelope envelope, final String crownCourtCrestId) {

        final JsonObject queryParameters = createObjectBuilder().build();

        final Envelope<JsonObject> requestEnvelope = Enveloper.envelop(queryParameters)
                .withName(REFERENCEDATA_QUERY_XHIBIT_COURT_MAPPINGS)
                .withMetadataFrom(envelope);

        return requester.requestAsAdmin(envelopeFrom(requestEnvelope.metadata(), requestEnvelope.payload()))
                .payloadAsJsonObject().getJsonArray("cpXhibitCourtMappings").getValuesAs(JsonObject.class)
                .stream()
                .filter(court -> court.getString("crestCourtId").equals(crownCourtCrestId))
                .map(this::createCourtLocation)
                .collect(Collectors.toList());
    }

    private UUID getOrganisationUnitId(final Envelope envelope, final String ouCode) {
        final JsonObject queryParameters = createObjectBuilder().add("oucode", ouCode).build();

        final Envelope<JsonObject> requestEnvelope = Enveloper.envelop(queryParameters)
                .withName(REFERENCEDATA_QUERY_ORGANISATION_UNITS)
                .withMetadataFrom(envelope);

        final JsonObject organisationUnit = requester.requestAsAdmin(envelopeFrom(requestEnvelope.metadata(), requestEnvelope.payload()))
                .payloadAsJsonObject().getJsonArray("organisationunits").getValuesAs(JsonObject.class)
                .stream().findFirst().orElseThrow(() -> new RuntimeException(format("Cannot find organisation unit with code %s", ouCode)));

        return UUID.fromString(organisationUnit.getString("id"));
    }

    public CourtLocation getCourtDetails(final Envelope envelope, final UUID courtCentreId) {
        final JsonObject queryParameters = createObjectBuilder().add("ouId", courtCentreId.toString()).build();

        final Envelope<JsonObject> requestEnvelope = Enveloper.envelop(queryParameters)
                .withName(REFERENCEDATA_QUERY_XHIBIT_COURT_MAPPINGS)
                .withMetadataFrom(envelope);

        final JsonObject court = requester.requestAsAdmin(envelopeFrom(requestEnvelope.metadata(), requestEnvelope.payload()))
                .payloadAsJsonObject().getJsonArray("cpXhibitCourtMappings").getValuesAs(JsonObject.class)
                .stream().findFirst().orElseThrow(() -> new RuntimeException(format("Cannot find court details with courtCentre %s", courtCentreId)));

        return createCourtLocation(court);
    }

    public int getCourtRoomNumber(final JsonEnvelope envelope, final UUID courtCentreId, final UUID courtRoomId) {

        final JsonObject queryParameters = createObjectBuilder().add("id", courtCentreId.toString()).build();

        final Envelope<JsonObject> requestEnvelope = Enveloper.envelop(queryParameters)
                .withName(REFERENCEDATA_QUERY_COURTROOM)
                .withMetadataFrom(envelope);

        final JsonObject courtRoom = requester.requestAsAdmin(envelopeFrom(requestEnvelope.metadata(), requestEnvelope.payload()))
                .payloadAsJsonObject().getJsonArray("courtrooms").getValuesAs(JsonObject.class)
                .stream().filter(c -> UUID.fromString(c.getString("id")).equals(courtRoomId))
                .findFirst()
                .orElse(Json.createObjectBuilder().add("courtroomId", UNMAPPED_COURT_ROOM_NUMBER).build());

        return courtRoom.getInt("courtroomId");
    }

    public JsonObject getXhibitHearingType(final JsonEnvelope envelope, final UUID cppHearingTypeId) {
        final Envelope<JsonObject> requestEnvelope = Enveloper.envelop(createObjectBuilder().build())
                .withName(REFERENCE_DATA_HEARING_TYPES)
                .withMetadataFrom(envelope);

        return requester.requestAsAdmin(envelopeFrom(requestEnvelope.metadata(), requestEnvelope.payload()))
                .payloadAsJsonObject()
                .getJsonArray("hearingTypes").getValuesAs(JsonObject.class).stream()
                .filter(h -> UUID.fromString(h.getString("id")).equals(cppHearingTypeId))
                .findFirst().orElseThrow(() -> new RuntimeException(format("Cannot find hearing type %s", cppHearingTypeId)));
    }

    private CourtLocation createCourtLocation(final JsonObject jsonObject) {
        return new CourtLocation(
                jsonObject.getString("oucode"),
                jsonObject.getString("crestCourtId"),
                jsonObject.getString("crestCourtSiteId", null),
                jsonObject.getString("crestCourtName", null),
                jsonObject.getString("crestCourtShortName", null),
                jsonObject.getString("crestCourtSiteName", null),
                jsonObject.getString("crestCourtSiteCode", null),
                jsonObject.getString("courtType", null));
    }
}
