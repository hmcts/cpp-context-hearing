package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.listing.common.xhibit.exception.InvalidReferenceDataException;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtMapping;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtMappingsList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtRoomMappingsList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.HearingTypesList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.JudiciariesList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.Judiciary;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.OrganisationUnit;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.OrganisationUnitList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.commons.collections.CollectionUtils;

@SuppressWarnings({"squid:CallToDeprecatedMethod"})
@ApplicationScoped
public class ReferenceDataLoader {

    private static final String REFERENCEDATA_QUERY_ORGANISATION_UNITS = "referencedata.query.organisationunits";
    private static final String REFERENCEDATA_QUERY_XHIBIT_COURT_MAPPINGS = "referencedata.query.cp-xhibit-court-mappings";
    private static final String REFERENCEDATA_QUERY_XHIBIT_MAGS_COURT_MAPPINGS = "referencedata.query.cp-xhibit-mags-court-mapping";
    private static final String REFERENCEDATA_QUERY_CP_XHIBIT_COURTROOM_MAPPINGS = "referencedata.query.cp-xhibit-courtroom-mappings";
    private static final String REFERENCEDATA_QUERY_JUDICIARIES = "referencedata.query.judiciaries";

    private static final String REFERENCE_DATA_ALL_HEARING_TYPES = "referencedata.query.all-hearing-types";
    private static final String REFERENCEDATA_QUERY_COURTROOM = "referencedata.query.courtroom";

    private static final String XHIBIT_COURT_MAPPINGS_QUERY_PARAM = "ouId";
    private static final String REFERENCEDATA_OUCODE_QUERY_PARAM = "oucode";

    @ServiceComponent(EVENT_PROCESSOR)
    @Inject
    private Requester requester;

    public Optional<OrganisationUnit> getOrganisationUnitByOuCode(final String ouCode) {
        final JsonObject queryParameters = createObjectBuilder().add(REFERENCEDATA_OUCODE_QUERY_PARAM, ouCode).build();

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(REFERENCEDATA_QUERY_ORGANISATION_UNITS)
                        .withId(randomUUID())
                        .build(),
                queryParameters);

        final Envelope<OrganisationUnitList> response = requester.requestAsAdmin(requestEnvelope, OrganisationUnitList.class);

        return (!Objects.isNull(response) && !Objects.isNull(response.payload()) && !Objects.isNull(response.payload().getOrganisationunits()) && !response.payload().getOrganisationunits().isEmpty()) ?
                of(response.payload().getOrganisationunits().get(0)) : empty();
    }

    public List<OrganisationUnit> fetchOrganisationUnitsByOucodeL2Code(final String oucodeL2Code) {
        final JsonObject queryParameters = createObjectBuilder().add("oucodeL2Code", oucodeL2Code).build();

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(REFERENCEDATA_QUERY_ORGANISATION_UNITS)
                        .withId(randomUUID())
                        .build(),
                queryParameters);

        final Envelope<OrganisationUnitList> response = requester.requestAsAdmin(requestEnvelope, OrganisationUnitList.class);

        return response.payload().getOrganisationunits();
    }

    public Optional<OrganisationUnitList> getOrganisationUnitList() {

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(REFERENCEDATA_QUERY_ORGANISATION_UNITS)
                        .withId(randomUUID())
                        .build(),
                createObjectBuilder().build());

        final Envelope<OrganisationUnitList> response = requester.requestAsAdmin(requestEnvelope, OrganisationUnitList.class);

        if (Objects.isNull(response) || Objects.isNull(response.payload()) || CollectionUtils.isEmpty(response.payload().getOrganisationunits())) {
            throw new InvalidReferenceDataException("Cannot find organisationunits");
        }

        return of(response.payload());
    }

    public Optional<CourtMappingsList> getXhibitCrownCourtMappings(final UUID courtCentreId) {
        final JsonObject queryParameters = createObjectBuilder().add(XHIBIT_COURT_MAPPINGS_QUERY_PARAM, courtCentreId.toString()).build();

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(REFERENCEDATA_QUERY_XHIBIT_COURT_MAPPINGS)
                        .withId(randomUUID())
                        .build(),
                queryParameters);

        final Envelope<CourtMappingsList> response = requester.requestAsAdmin(requestEnvelope, CourtMappingsList.class);

        return Objects.isNull(response) ? empty() : of(response.payload());
    }

    public Optional<CourtMappingsList> getXhibitCrownCourtMappings() {
        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(REFERENCEDATA_QUERY_XHIBIT_COURT_MAPPINGS)
                        .withId(randomUUID())
                        .build(),
                createObjectBuilder().build());

        final Envelope<CourtMappingsList> response = requester.requestAsAdmin(requestEnvelope, CourtMappingsList.class);

        return Objects.isNull(response) ? empty() : of(response.payload());
    }

    public Optional<CourtMappingsList> getXhibitMagsCourtMappings(final String oucode) {
        final JsonObject queryParameters = createObjectBuilder().add(REFERENCEDATA_OUCODE_QUERY_PARAM, oucode).build();

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(REFERENCEDATA_QUERY_XHIBIT_MAGS_COURT_MAPPINGS)
                        .withId(randomUUID())
                        .build(),
                queryParameters);

        final Envelope<CourtMapping> response = requester.requestAsAdmin(requestEnvelope, CourtMapping.class);

        return Objects.isNull(response) ? empty() : of((new CourtMappingsList(asList(response.payload()))));
    }

    public Optional<CourtRoomMappingsList> getCourtRoomMappingsList(UUID courtCentreId) {
        final JsonObject queryParameters = createObjectBuilder().add(XHIBIT_COURT_MAPPINGS_QUERY_PARAM, courtCentreId.toString()).build();

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(REFERENCEDATA_QUERY_CP_XHIBIT_COURTROOM_MAPPINGS)
                        .withId(randomUUID())
                        .build(),
                queryParameters);

        final Envelope<CourtRoomMappingsList> response = requester.requestAsAdmin(requestEnvelope, CourtRoomMappingsList.class);

        return Objects.isNull(response) ? empty() : of(response.payload());
    }

    public Optional<HearingTypesList> getAllHearingTypesList() {
        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(REFERENCE_DATA_ALL_HEARING_TYPES)
                        .withId(randomUUID())
                        .build(),
                createObjectBuilder().build());

        final Envelope<HearingTypesList> response = requester.requestAsAdmin(requestEnvelope, HearingTypesList.class);

        return Objects.isNull(response) ? empty() : of(response.payload());
    }

    public Optional<Judiciary> getJudiciary(UUID judiciaryId) {
        final JsonObject queryParameters = createObjectBuilder().add("ids", judiciaryId.toString()).build();

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(REFERENCEDATA_QUERY_JUDICIARIES)
                        .withId(randomUUID())
                        .build(),
                queryParameters);

        final Envelope<JudiciariesList> response = requester.requestAsAdmin(requestEnvelope, JudiciariesList.class);

        return Objects.isNull(response) ? empty() : of(response.payload().getJudiciaries().get(0));
    }

    public List<JsonObject> getCpCourtRoom(final UUID courtCentreId) {
        final JsonObject queryParameters = createObjectBuilder().add("id", courtCentreId.toString()).build();

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder().
                        withId(randomUUID()).
                        withName(REFERENCEDATA_QUERY_COURTROOM),
                queryParameters);

        final JsonArray courtRooms = requester.requestAsAdmin(requestEnvelope).payloadAsJsonObject().getJsonArray("courtrooms");

        if (isEmpty(courtRooms)) {
            throw new InvalidReferenceDataException("Cannot find court room with courtCentreId " + courtCentreId.toString());
        }

        return courtRooms.getValuesAs(JsonObject.class);
    }
}