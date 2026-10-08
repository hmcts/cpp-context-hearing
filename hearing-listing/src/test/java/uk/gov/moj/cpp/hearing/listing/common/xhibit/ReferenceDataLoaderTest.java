package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.listing.common.utils.FileUtil.givenPayload;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.listing.common.xhibit.exception.InvalidReferenceDataException;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtMapping;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtMappingsList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtRoomMappingsList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.HearingType;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.HearingTypesList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.JudiciariesList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.Judiciary;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.OrganisationUnit;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.OrganisationUnitList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReferenceDataLoaderTest {

    @InjectMocks
    private ReferenceDataLoader referenceDataLoader;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Requester requester;

    @Spy
    private JsonObjectToObjectConverter jsonObjectConverter;

    private static final String VENUE_NAME_FIELD = "venueName";
    private static final String COURT_ROOM_ID_FIELD = "courtroomId";
    private static final String COURT_ROOM_NAME_FIELD = "courtroomName";
    private static final String COURT_ROOMS_FIELD = "courtrooms";

    @BeforeEach
    public void init() {
        setField(this.jsonObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldGetOrganisationUnitByOuCode() {
        final UUID ouId = randomUUID();
        final String ouCode = "B01LY00";

        final OrganisationUnit organisationUnit = new OrganisationUnit.Builder()
                .withId(ouId)
                .withOucode(ouCode)
                .build();

        when(requester.requestAsAdmin(any(), eq(OrganisationUnitList.class)).payload()).thenReturn(new OrganisationUnitList(Arrays.asList(organisationUnit)));

        final Optional<OrganisationUnit> actualOrganisationUnit = referenceDataLoader.getOrganisationUnitByOuCode(ouCode);


        assertThat(true, equalTo(actualOrganisationUnit.isPresent()));
        assertThat(ouId, is(actualOrganisationUnit.get().getId()));
    }

    @Test
    public void shouldGetOrganisationUnitByOuCode1() {
        final UUID ouId = randomUUID();
        final String ouCode = "B01LY00";

        final OrganisationUnit organisationUnit = new OrganisationUnit.Builder()
                .withId(ouId)
                .withOucode(ouCode)
                .build();

        when(requester.requestAsAdmin(any(), eq(OrganisationUnitList.class)).payload()).thenReturn(new OrganisationUnitList(Arrays.asList(organisationUnit)));

        final List<OrganisationUnit> organisationUnits = referenceDataLoader.fetchOrganisationUnitsByOucodeL2Code(ouCode);


        assertThat(ouId, is(organisationUnits.get(0).getId()));
    }

    @Test
    public void shouldGetOrganisationUnitList() {
        final UUID ouId = randomUUID();
        final String ouCode = "B01LY00";

        final OrganisationUnit organisationUnit = new OrganisationUnit.Builder()
                .withId(ouId)
                .withOucode(ouCode)
                .build();

        when(requester.requestAsAdmin(any(), eq(OrganisationUnitList.class)).payload()).thenReturn(new OrganisationUnitList(Arrays.asList(organisationUnit)));

        final Optional<OrganisationUnitList> actualOrganisationUnit = referenceDataLoader.getOrganisationUnitList();


        assertThat(true, equalTo(actualOrganisationUnit.isPresent()));
        assertThat(ouId, is(actualOrganisationUnit.get().getOrganisationunits().get(0).getId()));
        assertThat(ouCode, is(actualOrganisationUnit.get().getOrganisationunits().get(0).getOucode()));
    }

    @Test
    public void shouldNotFailIfOrganisationUnitByOuCodeIsEmpty() {
        final String ouCode = "B01LY00";

        when(requester.requestAsAdmin(any(), eq(OrganisationUnitList.class)).payload()).thenReturn(new OrganisationUnitList(Arrays.asList()));

        final Optional<OrganisationUnit> actualOrganisationUnit = referenceDataLoader.getOrganisationUnitByOuCode(ouCode);

        assertThat(false, equalTo(actualOrganisationUnit.isPresent()));
    }

    @Test
    public void shouldNotFailIfResponseIsNull() {
        final String ouCode = "B01LY00";

        when(requester.requestAsAdmin(any(), any())).thenReturn(null);

        final Optional<OrganisationUnit> actualOrganisationUnit = referenceDataLoader.getOrganisationUnitByOuCode(ouCode);

        assertThat(false, equalTo(actualOrganisationUnit.isPresent()));
    }


    @Test
    public void shouldNotFailIfOrganisationUnitByOuCodePayloadIsNull() {
        final String ouCode = "B01LY00";

        when(requester.requestAsAdmin(any(), any()).payload()).thenReturn(null);

        final Optional<OrganisationUnit> actualOrganisationUnit = referenceDataLoader.getOrganisationUnitByOuCode(ouCode);

        assertThat(false, equalTo(actualOrganisationUnit.isPresent()));
    }

    @Test
    public void shouldThrowInvalidReferenceDataExceptionIfPayloadIsNullWhenGetOrganisationUnitList() {
        when(requester.requestAsAdmin(any(), eq(OrganisationUnitList.class)).payload()).thenReturn(null);
        assertThrows(InvalidReferenceDataException.class, () -> referenceDataLoader.getOrganisationUnitList());
    }

    @Test
    public void shouldThrowInvalidReferenceDataExceptionIfEnvelopeIsNullWhenGetOrganisationUnitList() {
        when(requester.requestAsAdmin(any(), eq(OrganisationUnitList.class))).thenReturn(null);
        assertThrows(InvalidReferenceDataException.class, () -> referenceDataLoader.getOrganisationUnitList());
    }

    @Test
    public void shouldGetXhibitCourtMappings() {
        final String ouCode = "OUCODE";
        final String courtId = "432";
        final String courtSiteId = "433";
        final String crestCourtName = "BLACKFRIARS";
        final String courtSiteName = "BLACKFRIARS";
        final String courtShortName = "BLF";
        final String courtSiteCode = "B";
        final String courtType = "CROWN";

        final CourtMapping courtMapping = new CourtMapping.Builder()
                .withOucode(ouCode)
                .withCrestCourtId(courtId)
                .withCrestCourtSiteId(courtSiteId)
                .withCrestCourtName(crestCourtName)
                .withCrestCourtSiteName(courtSiteName)
                .withCrestCourtShortName(courtShortName)
                .withCrestCourtSiteCode(courtSiteCode)
                .withCourtType(courtType)
                .build();

        when(requester.requestAsAdmin(any(), eq(CourtMappingsList.class)).payload()).thenReturn(new CourtMappingsList(Arrays.asList(courtMapping)));

        final Optional<CourtMappingsList> courtMappingsList = referenceDataLoader.getXhibitCrownCourtMappings();

        assertThat(true, equalTo(courtMappingsList.isPresent()));
        assertThat(1, is(equalTo(courtMappingsList.get().getCpXhibitCourtMappings().size())));
        assertThat(ouCode, is(courtMappingsList.get().getCpXhibitCourtMappings().get(0).getOucode()));
        assertThat(courtId, is(courtMappingsList.get().getCpXhibitCourtMappings().get(0).getCrestCourtId()));
    }

    @Test
    public void shouldGetXhibitCourtMappingsWithCourtCenter() {
        final String ouCode = "OUCODE";
        final String courtId = "432";
        final String courtSiteId = "433";
        final String crestCourtName = "BLACKFRIARS";
        final String courtSiteName = "BLACKFRIARS";
        final String courtShortName = "BLF";
        final String courtSiteCode = "B";
        final String courtType = "CROWN";

        final CourtMapping courtMapping = new CourtMapping.Builder()
                .withOucode(ouCode)
                .withCrestCourtId(courtId)
                .withCrestCourtSiteId(courtSiteId)
                .withCrestCourtName(crestCourtName)
                .withCrestCourtSiteName(courtSiteName)
                .withCrestCourtShortName(courtShortName)
                .withCrestCourtSiteCode(courtSiteCode)
                .withCourtType(courtType)
                .build();

        when(requester.requestAsAdmin(any(), eq(CourtMappingsList.class)).payload()).thenReturn(new CourtMappingsList(Arrays.asList(courtMapping)));

        final Optional<CourtMappingsList> courtMappingsList = referenceDataLoader.getXhibitCrownCourtMappings(UUID.randomUUID());

        assertThat(true, equalTo(courtMappingsList.isPresent()));
        assertThat(1, is(equalTo(courtMappingsList.get().getCpXhibitCourtMappings().size())));
        assertThat(ouCode, is(courtMappingsList.get().getCpXhibitCourtMappings().get(0).getOucode()));
        assertThat(courtId, is(courtMappingsList.get().getCpXhibitCourtMappings().get(0).getCrestCourtId()));
    }

    @Test
    public void shouldGetXhibitMagsCourtMappings() {
        final String ouCode = "OUCODE";
        final String courtId = "432";
        final String courtSiteId = "433";
        final String crestCourtName = "BLACKFRIARS";
        final String courtSiteName = "BLACKFRIARS";
        final String courtShortName = "BLF";
        final String courtSiteCode = "B";
        final String courtType = "MAGISTRATE";

        final CourtMapping courtMapping = new CourtMapping.Builder()
                .withOucode(ouCode)
                .withCrestCourtId(courtId)
                .withCrestCourtSiteId(courtSiteId)
                .withCrestCourtName(crestCourtName)
                .withCrestCourtSiteName(courtSiteName)
                .withCrestCourtShortName(courtShortName)
                .withCrestCourtSiteCode(courtSiteCode)
                .withCourtType(courtType)
                .build();

        when(requester.requestAsAdmin(any(), eq(CourtMapping.class)).payload()).thenReturn(courtMapping);

        final Optional<CourtMappingsList> courtMappingsList = referenceDataLoader.getXhibitMagsCourtMappings(ouCode);

        assertThat(true, equalTo(courtMappingsList.isPresent()));
        assertThat(1, is(equalTo(courtMappingsList.get().getCpXhibitCourtMappings().size())));
        assertThat(ouCode, is(courtMappingsList.get().getCpXhibitCourtMappings().get(0).getOucode()));
        assertThat(courtId, is(courtMappingsList.get().getCpXhibitCourtMappings().get(0).getCrestCourtId()));
    }

    @Test
    public void shouldGetCourtRoomMappingsList() {
        final UUID courtCentreId = randomUUID();
        final JsonObject courtRoomMappingJson = givenPayload("/mock-data/referencedata.query.cp-xhibit-courtroom-mappings.json");
        final CourtRoomMappingsList expectedCourtRoomMappingsList = jsonObjectConverter.convert(courtRoomMappingJson, CourtRoomMappingsList.class);

        when(requester.requestAsAdmin(any(), eq(CourtRoomMappingsList.class)).payload()).thenReturn(expectedCourtRoomMappingsList);

        final Optional<CourtRoomMappingsList> actualCourtRoomMappingsList = referenceDataLoader.getCourtRoomMappingsList(courtCentreId);

        assertThat(true, equalTo(actualCourtRoomMappingsList.isPresent()));
        assertThat(expectedCourtRoomMappingsList.getCpXhibitCourtRoomMappings().size(), is(equalTo(actualCourtRoomMappingsList.get().getCpXhibitCourtRoomMappings().size())));
        assertThat(expectedCourtRoomMappingsList.getCpXhibitCourtRoomMappings().get(0).getOucode(), is(actualCourtRoomMappingsList.get().getCpXhibitCourtRoomMappings().get(0).getOucode()));
        assertThat(expectedCourtRoomMappingsList.getCpXhibitCourtRoomMappings().get(0).getCrestCourtId(), is(actualCourtRoomMappingsList.get().getCpXhibitCourtRoomMappings().get(0).getCrestCourtId()));
    }

    @Test
    public void shouldGetAllHearingTypesList() {
        final UUID cppHearingTypeId = randomUUID();

        final HearingType hearingType = new HearingType.Builder()
                .withId(cppHearingTypeId)
                .build();

        final HearingTypesList expectedHearingTypesList = new HearingTypesList(Arrays.asList(hearingType));

        when(requester.requestAsAdmin(any(), eq(HearingTypesList.class)).payload()).thenReturn(expectedHearingTypesList);

        final Optional<HearingTypesList> actualHearingTypesList = referenceDataLoader.getAllHearingTypesList();

        assertThat(true, equalTo(actualHearingTypesList.isPresent()));
        assertThat(expectedHearingTypesList.getHearingTypes().size(), is(equalTo(actualHearingTypesList.get().getHearingTypes().size())));
        assertThat(expectedHearingTypesList.getHearingTypes().get(0).getId(), equalTo(actualHearingTypesList.get().getHearingTypes().get(0).getId()));
    }

    @Test
    public void shouldGetJudiciariesList() {
        final String titlePrefix = "Mr";
        final String titleJudicialPrefix = "Recorder";
        final String judiciaryType = "Recorder";
        final UUID judiciaryId = randomUUID();

        final Judiciary expectedJudiciary = new Judiciary.Builder()
                .withId(judiciaryId)
                .withTitlePrefix(titlePrefix)
                .withTitleJudicialPrefix(titleJudicialPrefix)
                .withJudiciaryType(judiciaryType)
                .build();

        final JudiciariesList expectedJudiciariesList = new JudiciariesList(Arrays.asList(expectedJudiciary));

        when(requester.requestAsAdmin(any(), eq(JudiciariesList.class)).payload()).thenReturn(expectedJudiciariesList);

        final Optional<Judiciary> actualJudiciary = referenceDataLoader.getJudiciary(judiciaryId);

        assertThat(true, equalTo(actualJudiciary.isPresent()));
        assertThat(expectedJudiciary.getId(), equalTo(actualJudiciary.get().getId()));
        assertThat(expectedJudiciary.getTitlePrefix(), equalTo(actualJudiciary.get().getTitlePrefix()));
        assertThat(expectedJudiciary.getTitleJudicialPrefix(), equalTo(actualJudiciary.get().getTitleJudicialPrefix()));
    }

    @Test
    public void shouldGetCpCourtRoom() {
        final UUID courtCentreId = randomUUID();
        final JsonObject courtRooms = getPayloadForCourtRooms(courtCentreId.toString());
        final List<JsonObject> expectedCourtRoomList = courtRooms.getJsonArray(COURT_ROOMS_FIELD).getValuesAs(JsonObject.class);

        when(requester.requestAsAdmin(any()).payloadAsJsonObject().getJsonArray(COURT_ROOMS_FIELD)).thenReturn(courtRooms.getJsonArray(COURT_ROOMS_FIELD));

        final List<JsonObject> actualCpCourtRoomList = referenceDataLoader.getCpCourtRoom(courtCentreId);

        assertThat(true, equalTo(Objects.nonNull(actualCpCourtRoomList)));
        assertThat(expectedCourtRoomList.size(), is(equalTo(actualCpCourtRoomList.size())));
        assertThat(expectedCourtRoomList.get(0).get("id"), equalTo(actualCpCourtRoomList.get(0).get("id")));
        assertThat(expectedCourtRoomList.get(0).get(VENUE_NAME_FIELD), equalTo(actualCpCourtRoomList.get(0).get(VENUE_NAME_FIELD)));
        assertThat(expectedCourtRoomList.get(0).get(COURT_ROOM_ID_FIELD), equalTo(actualCpCourtRoomList.get(0).get(COURT_ROOM_ID_FIELD)));
        assertThat(expectedCourtRoomList.get(0).get(COURT_ROOM_NAME_FIELD), equalTo(actualCpCourtRoomList.get(0).get(COURT_ROOM_NAME_FIELD)));
    }

    private JsonObject getPayloadForCourtRooms(String id) {
        return Json.createObjectBuilder()
                .add("id", id)
                .add("oucode", "B01LY00")
                .add("oucodeL3Name", "South Western (Lavender Hill)")
                .add("oucodeL3WelshName", "welshName_Test")
                .add(COURT_ROOMS_FIELD, getCourtRooms())
                .build();
    }

    private JsonArray getCourtRooms() {
        return createArrayBuilder().add(createObjectBuilder()
                        .add("id", randomUUID().toString())
                        .add(VENUE_NAME_FIELD, "BEXLEY MAGISTRATES' COURT")
                        .add(COURT_ROOM_ID_FIELD, 12)
                        .add(COURT_ROOM_NAME_FIELD, "Courtroom 01"))
                .build();
    }

}
