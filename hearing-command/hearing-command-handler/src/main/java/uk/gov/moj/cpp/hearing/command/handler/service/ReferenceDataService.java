package uk.gov.moj.cpp.hearing.command.handler.service;

import uk.gov.justice.core.courts.YouthCourt;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.moj.cpp.hearing.domain.CourtCentre;

import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

public class ReferenceDataService {

    private static final String REFERENCEDATA_QUERY_COURT_CENTRES = "referencedata.query.courtrooms";
    private static final String REFERENCEDATA_QUERY_YOUTH_COURT = "referencedata.query.youth-courts-by-mag-uuid";


    private static final String REFERENCEDATA_QUERY_PLEA_TYPES = "referencedata.query.plea-types";
    private static final String FIELD_PLEA_STATUS_TYPES = "pleaStatusTypes";
    private static final String FIELD_PLEA_TYPE_GUILTY_FLAG = "pleaTypeGuiltyFlag";
    private static final String GUILTY_FLAG_YES = "Yes";
    private static final String FIELD_PLEA_VALUE = "pleaValue";

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
    @SuppressWarnings({"squid:S1854", "squid:S1481"})
    public YouthCourt getYouthCourtForMagistrateCourt (final UUID uuid) {
        final JsonObject payload = createObjectBuilder()
                .add("magsUUID", uuid.toString())
                .build();

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(REFERENCEDATA_QUERY_YOUTH_COURT)
                        .withId(uuid)
                        .build(),
                payload);

        final JsonArray youthCourtJsonArray = requester.requestAsAdmin(requestEnvelope).payloadAsJsonObject().getJsonArray("youthCourts");
        final RefDataYouthCourt refDataYouthCourt =  youthCourtJsonArray.stream().map(i -> stringToJsonObjectConverter.convert(i.toString())).map(i -> jsonObjectToObjectConverter.convert(i, RefDataYouthCourt.class)).findFirst().orElse(null);
        return  refDataYouthCourt != null ? new YouthCourt(refDataYouthCourt.getCourtCode(), refDataYouthCourt.getCourtName(), refDataYouthCourt.getCourtNameWelsh(), refDataYouthCourt.getId()): null;
    }

    public  static class RefDataYouthCourt {
        private Integer courtCode;

        private String courtName;

        private String courtNameWelsh;

        private UUID id;

        public RefDataYouthCourt(Integer courtCode, String courtName, String courtNameWelsh, UUID id) {
            this.courtCode = courtCode;
            this.courtName = courtName;
            this.courtNameWelsh = courtNameWelsh;
            this.id = id;
        }

        public Integer getCourtCode() {
            return courtCode;
        }

        public void setCourtCode(Integer courtCode) {
            this.courtCode = courtCode;
        }

        public String getCourtName() {
            return courtName;
        }

        public void setCourtName(String courtName) {
            this.courtName = courtName;
        }

        public String getCourtNameWelsh() {
            return courtNameWelsh;
        }

        public void setCourtNameWelsh(String courtNameWelsh) {
            this.courtNameWelsh = courtNameWelsh;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }
    }


    public Optional<CourtCentre> resolveCourtCentre(final UUID courtCentreId, final UUID courtRoomId) {
        if (courtCentreId == null || courtRoomId == null) {
            return Optional.empty();
        }

        final JsonArray organisationUnits = queryCourtCentresFor(courtCentreId);
        if (organisationUnits == null || organisationUnits.isEmpty()) {
            return Optional.empty();
        }

        return findMatchingOrganisationUnit(organisationUnits, courtCentreId)
                .flatMap(ou -> buildCourtCentreFromRoom(ou, courtCentreId, courtRoomId));
    }

    private JsonArray queryCourtCentresFor(final UUID courtCentreId) {
        final JsonObject payload = createObjectBuilder()
                .add("courtCentreId", courtCentreId.toString())
                .build();

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(REFERENCEDATA_QUERY_COURT_CENTRES)
                        .withId(randomUUID())
                        .build(),
                payload);

        return requester.requestAsAdmin(requestEnvelope)
                .payloadAsJsonObject()
                .getJsonArray("organisationunits");
    }

    private Optional<JsonObject> findMatchingOrganisationUnit(final JsonArray organisationUnits, final UUID courtCentreId) {
        final String idAsString = courtCentreId.toString();
        return organisationUnits.getValuesAs(JsonObject.class).stream()
                .filter(unit -> idAsString.equals(getStringOrNull(unit, "id")))
                .findFirst();
    }

    private Optional<CourtCentre> buildCourtCentreFromRoom(final JsonObject ou, final UUID courtCentreId, final UUID courtRoomId) {
        final JsonArray courtrooms = ou.getJsonArray("courtrooms");
        if (isNull(courtrooms)) {
            return Optional.empty();
        }

        return courtrooms.getValuesAs(JsonObject.class).stream()
                .filter(room -> roomIdMatches(room, courtRoomId))
                .findFirst()
                .map(room -> CourtCentre.courtCentre()
                        .withId(courtCentreId)
                        .withName(getStringOrNull(ou, "oucodeL3Name"))
                        .withWelshName(getStringOrNull(ou, "oucodeL3WelshName"))
                        .withRoomId(courtRoomId)
                        .withRoomName(getStringOrNull(room, "courtroomName"))
                        .withWelshRoomName(getStringOrNull(room, "welshCourtroomName"))
                        .build());
    }

    private static boolean roomIdMatches(final JsonObject room, final UUID courtRoomId) {
        final String id = getStringOrNull(room, "id");
        return id != null && courtRoomId.equals(UUID.fromString(id));
    }

    private static String getStringOrNull(final JsonObject json, final String key) {
        return json.containsKey(key) ? json.getString(key) : null;
    }

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
