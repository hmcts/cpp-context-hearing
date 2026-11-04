package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static java.util.Arrays.asList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;
import static uk.gov.moj.cpp.hearing.listing.common.utils.FileUtil.givenPayload;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.listing.common.xhibit.exception.InvalidReferenceDataException;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtMapping;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtMappingsList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtRoomMapping;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtRoomMappingsList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.HearingType;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.HearingTypesList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.Judiciary;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.OrganisationUnit;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.OrganisationUnitList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReferenceDataCacheTest {

    @Mock
    private ReferenceDataLoader referenceDataLoader;

    @InjectMocks
    private ReferenceDataCache referenceDataCache;

    @Spy
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    private static Optional<HearingTypesList> hearingTypesList;
    private static Optional<CourtMappingsList> crownCourtMappingsList;
    private static Optional<CourtMappingsList> magsCourtMappingsList;
    private static Optional<OrganisationUnitList> organisationUnitList;

    private static final String COURT_ID = "432";
    private static final UUID HEARING_TYPE_ID = randomUUID();
    private static final UUID COURT_CENTRE_ID = randomUUID();
    private static final String OU_CODE = "C01BL00";

    private static final String VENUE_NAME_FIELD = "venueName";
    private static final String COURT_ROOM_ID_FIELD = "courtroomId";
    private static final String COURT_ROOM_NAME_FIELD = "courtroomName";


    @Test
    public void shouldPopulateOrganisationUnitsCache() {

        initializeTestData();
        referenceDataCache.initReferenceData();

        final OrganisationUnit expectedOrganisationUnit = getOrganisationUnit();
        
        when(referenceDataLoader.getOrganisationUnitByOuCode(OU_CODE)).thenReturn(Optional.of(expectedOrganisationUnit));

        final Optional<OrganisationUnit> actualOrganisationUnit = referenceDataCache.getOrganisationUnitMapCache(OU_CODE);

        assertThat(actualOrganisationUnit.isPresent(), is(true));
        assertThat(actualOrganisationUnit.get().getOucode(), is(expectedOrganisationUnit.getOucode()));
        assertThat(actualOrganisationUnit.get().getId(), is(expectedOrganisationUnit.getId()));
    }

    @Test
    public void shouldPopulateCourtMappingsCache() {
        initializeTestData();
        referenceDataCache.initReferenceData();

        final CourtMapping expectedCourtMapping = getCourtMapping("CROWN_COURT");
        when(referenceDataLoader.getXhibitCrownCourtMappings()).thenReturn(crownCourtMappingsList);

        assertThat(referenceDataCache.getCrownCourtLocationsCache(COURT_ID).get(0).getCourtSiteCode(), is(expectedCourtMapping.getCrestCourtSiteCode()));
    }

    @Test
    public void shouldPopulateCourtRoomMappingsCache() {
        final UUID courtCentreId = randomUUID();

        initializeTestData();
        referenceDataCache.initReferenceData();

        final JsonObject courtRoomMappingJson = givenPayload("/mock-data/referencedata.query.cp-xhibit-courtroom-mappings.json");
        final Optional<CourtRoomMappingsList> expectedCourtRoomMappingsList =  Optional.of(jsonObjectConverter.convert(courtRoomMappingJson, CourtRoomMappingsList.class));

        when(referenceDataLoader.getCourtRoomMappingsList(courtCentreId)).thenReturn(expectedCourtRoomMappingsList);

        final CourtRoomMappingsList actualCourtRoomMappingsList = referenceDataCache.getCourtRoomMappingsMapCache(courtCentreId);

        assertThat(actualCourtRoomMappingsList.getCpXhibitCourtRoomMappings().size(), is(expectedCourtRoomMappingsList.get().getCpXhibitCourtRoomMappings().size()));
        assertThat(actualCourtRoomMappingsList.getCpXhibitCourtRoomMappings().get(0).getCourtRoomId(), is(expectedCourtRoomMappingsList.get().getCpXhibitCourtRoomMappings().get(0).getCourtRoomId()));
    }

    @Test
    public void shouldThrowInvalidReferenceDataException() {
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();

        initializeTestData();
        referenceDataCache.initReferenceData();

        when(referenceDataLoader.getCourtRoomMappingsList(courtCentreId)).thenReturn(Optional.empty());

        assertThrows(InvalidReferenceDataException.class, () -> referenceDataCache.getCourtRoomMappingByCourtCentreAndCourtRoom(courtCentreId, courtRoomId));
    }

    @Test
    public void shouldGetCourtRoomMappingByCourtCentreRoom() {
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = fromString("b3c9eb70-93eb-4570-a1fa-7516a5e4e9cd");

        initializeTestData();
        referenceDataCache.initReferenceData();


        final JsonObject courtRoomMappingJson = givenPayload("/mock-data/referencedata.query.cp-xhibit-courtroom-mappings.json");
        final CourtRoomMappingsList courtRoomMappingsList = jsonObjectConverter.convert(courtRoomMappingJson, CourtRoomMappingsList.class);

        when(referenceDataLoader.getCourtRoomMappingsList(courtCentreId)).thenReturn(Optional.of(courtRoomMappingsList));

        Optional<CourtRoomMapping> actualCourtRoomMappingOptional = referenceDataCache.getCourtRoomMappingByCourtCentreAndCourtRoom(courtCentreId, courtRoomId);

        assertThat(actualCourtRoomMappingOptional.isPresent(), is(true));
        assertThat(actualCourtRoomMappingOptional.get().getId(), is(courtRoomMappingsList.getCpXhibitCourtRoomMappings().get(0).getId()));
        assertThat(actualCourtRoomMappingOptional.get().getCourtRoomUUID(), is(courtRoomId));
        assertThat(actualCourtRoomMappingOptional.get().getCrestCourtId(), is(courtRoomMappingsList.getCpXhibitCourtRoomMappings().get(0).getCrestCourtId()));
        assertThat(actualCourtRoomMappingOptional.get().getCrestCourtRoomName(), is(courtRoomMappingsList.getCpXhibitCourtRoomMappings().get(0).getCrestCourtRoomName()));
    }

    @Test
    public void shouldGetEmptyWhenGettingCourtRoomMappingByCourtCentreRoom() {
        final UUID courtCentreId = randomUUID();
        final UUID wrongCourtRoomId = fromString("b3c9eb70-93eb-4570-a1fa-7516a5e4e9ce");

        initializeTestData();
        referenceDataCache.initReferenceData();

        final JsonObject courtRoomMappingJson = givenPayload("/mock-data/referencedata.query.cp-xhibit-courtroom-mappings.json");
        final CourtRoomMappingsList courtRoomMappingsList = jsonObjectConverter.convert(courtRoomMappingJson, CourtRoomMappingsList.class);

        when(referenceDataLoader.getCourtRoomMappingsList(courtCentreId)).thenReturn(Optional.of(courtRoomMappingsList));

        Optional<CourtRoomMapping> actualCourtRoomMappingOptional = referenceDataCache.getCourtRoomMappingByCourtCentreAndCourtRoom(courtCentreId, wrongCourtRoomId);

        assertThat(actualCourtRoomMappingOptional.isPresent(), is(false));
    }

    @Test
    public void shouldPopulateHearingTypesCache() {

        initializeTestData();
        referenceDataCache.initReferenceData();

        final HearingType expectedHearingType = getHearingType();

        when(referenceDataLoader.getAllHearingTypesList()).thenReturn(hearingTypesList);

        assertThat(referenceDataCache.getHearingTypeCache(HEARING_TYPE_ID).get().getId(), is(expectedHearingType.getId()));
    }

    @Test
    public void shouldPopulateFirstHearingTypeCache() {
        final HearingType expectedHearingType = new HearingType.Builder()
                .withId(HEARING_TYPE_ID)
                .withHearingCode("FHG")
                .withHearingDescription("FIRST HEARING")
                .build();

        initializeTestData();
        referenceDataCache.initReferenceData();


        when(referenceDataLoader.getAllHearingTypesList()).thenReturn(hearingTypesList);

        final HearingType actualHearingType = referenceDataCache.getHearingTypeCodeCache("FHG").get();
        assertThat(actualHearingType.getId(), is(expectedHearingType.getId()));
        assertThat(actualHearingType.getHearingCode(), is(expectedHearingType.getHearingCode()));
        assertThat(actualHearingType.getHearingDescription(), is(expectedHearingType.getHearingDescription()));
    }

    @Test
    public void shouldPopulateCrownCourtMappingsMapCache() {

        initializeTestData();

        when(referenceDataLoader.getAllHearingTypesList()).thenReturn(hearingTypesList);
        when(referenceDataLoader.getXhibitCrownCourtMappings()).thenReturn(crownCourtMappingsList);
        when(referenceDataLoader.getOrganisationUnitList()).thenReturn(organisationUnitList);

        referenceDataCache.initReferenceData();
        final List<CourtMapping> expectedCourtMappingList = asList(getCourtMapping("CROWN_COURT"));
        final Optional<List<CourtMapping>> actualCourtMappingList = referenceDataCache.getCrownCourtMappingsMapCache(COURT_CENTRE_ID);

        assertThat(actualCourtMappingList.isPresent(), is(true));
        assertThat(actualCourtMappingList.get().size(), is(expectedCourtMappingList.size()));
        assertThat(actualCourtMappingList.get().get(0).getId(), is(expectedCourtMappingList.get(0).getId()));
        assertThat(actualCourtMappingList.get().get(0).getOucode(), is(expectedCourtMappingList.get(0).getOucode()));
    }

    @Test
    public void shouldPopulateMagsCourtMappingsMapCache() {

        initializeTestData();

        when(referenceDataLoader.getAllHearingTypesList()).thenReturn(hearingTypesList);
        when(referenceDataLoader.getXhibitCrownCourtMappings()).thenReturn(crownCourtMappingsList);
        when(referenceDataLoader.getXhibitMagsCourtMappings(any())).thenReturn(magsCourtMappingsList);
        when(referenceDataLoader.getOrganisationUnitList()).thenReturn(organisationUnitList);

        referenceDataCache.initReferenceData();

        final List<CourtMapping> expectedCourtMappingList = asList(getCourtMapping("MAGISTRATES_COURT"));

        final Optional<List<CourtMapping>> actualCourtMappingList = referenceDataCache.getMagsCourtMappingsMapCache(COURT_CENTRE_ID);

        assertThat(actualCourtMappingList.isPresent(), is(true));
        assertThat(actualCourtMappingList.get().size(), is(expectedCourtMappingList.size()));
        assertThat(actualCourtMappingList.get().get(0).getId(), is(expectedCourtMappingList.get(0).getId()));
        assertThat(actualCourtMappingList.get().get(0).getOucode(), is(expectedCourtMappingList.get(0).getOucode()));
    }

    @Test
    public void shouldPopulateJudiciariesCache() {
        final UUID judiciaryId = randomUUID();

        final String titlePrefix = "Mr";
        final String titleJudicialPrefix = "Recorder";

        final Judiciary judiciary = new Judiciary.Builder()
                .withId(judiciaryId)
                .withTitlePrefix(titlePrefix)
                .withTitleJudicialPrefix(titleJudicialPrefix)
                .build();

        initializeTestData();
        referenceDataCache.initReferenceData();


        final Optional<Judiciary> expectedJudiciary = Optional.of(judiciary);

        when(referenceDataLoader.getJudiciary(judiciaryId)).thenReturn(expectedJudiciary);


        final Optional<Judiciary> actualJudiciary = referenceDataCache.getJudiciariesMapCache(judiciaryId);

        assertThat(actualJudiciary.isPresent(), is(true));
        assertThat(actualJudiciary.get().getTitlePrefix(), is(expectedJudiciary.get().getTitlePrefix()));
        assertThat(actualJudiciary.get().getTitleJudicialPrefix(), is(expectedJudiciary.get().getTitleJudicialPrefix()));
    }

    @Test
    public void shouldPopulateCpCourtRoomCache() {

        initializeTestData();
        referenceDataCache.initReferenceData();

        final UUID courtCentreId = randomUUID();
        final JsonObject courtRooms = getPayloadForCourtRooms(courtCentreId.toString());
        final List<JsonObject> courtRoomList = courtRooms.getJsonArray("courtrooms").getValuesAs(JsonObject.class);

        when(referenceDataLoader.getCpCourtRoom(courtCentreId)).thenReturn(courtRoomList);

        final List<JsonObject> actualCpCourtRoom = referenceDataCache.getCpCourtRoomCache(courtCentreId);

        assertThat(courtRoomList.size(), is(actualCpCourtRoom.size()));
        assertEquals(courtRoomList.get(0).get("id"), actualCpCourtRoom.get(0).get("id"));
        assertEquals(courtRoomList.get(0).get(VENUE_NAME_FIELD), actualCpCourtRoom.get(0).get(VENUE_NAME_FIELD));
        assertEquals(courtRoomList.get(0).get(COURT_ROOM_ID_FIELD), actualCpCourtRoom.get(0).get(COURT_ROOM_ID_FIELD));
        assertEquals(courtRoomList.get(0).get(COURT_ROOM_NAME_FIELD), actualCpCourtRoom.get(0).get(COURT_ROOM_NAME_FIELD));
    }

    @Test
    public void shouldInitAllHearingTypesRemovingDuplicates() {

        initializeTestData();
        referenceDataCache.initReferenceData();

        final HearingTypesList hearingTypeWithDuplicates = new HearingTypesList(asList(getHearingType(), getHearingType()));
        when(referenceDataLoader.getAllHearingTypesList()).thenReturn(Optional.of(hearingTypeWithDuplicates));

        referenceDataCache.initAllHearingTypes();

        Map<UUID, HearingType> actualCachedValues = getValueOfField(referenceDataCache, "hearingTypesMapCache", Map.class);
        assertThat(actualCachedValues.keySet(), hasSize(1));
    }

    @Test
    public void shouldInitOrganisationUnitsRemovingDuplicates() {

        initializeTestData();
        referenceDataCache.initReferenceData();

        final OrganisationUnitList orgUnitsWithDuplicates = new OrganisationUnitList(asList(getOrganisationUnit(), getOrganisationUnit()));
        when(referenceDataLoader.getOrganisationUnitList()).thenReturn(Optional.of(orgUnitsWithDuplicates));

        referenceDataCache.initOrganisationUnitList();

        Map<UUID, OrganisationUnit> actualCachedValues = getValueOfField(referenceDataCache, "organisationUnitMapByIdCache", Map.class);
        assertThat(actualCachedValues.keySet(), hasSize(1));
    }

    private void initializeTestData() {
        hearingTypesListTestData();
        courtMappingsListTestData();
        organisationUnitListTestData();
    }

    private void organisationUnitListTestData() {
        organisationUnitList = Optional.of(new OrganisationUnitList(asList(getOrganisationUnit())));
    }

    private OrganisationUnit getOrganisationUnit() {
        return new OrganisationUnit.Builder()
                .withId(COURT_CENTRE_ID)
                .withOucode(OU_CODE)
                .build();
    }

    private void hearingTypesListTestData() {
        hearingTypesList = Optional.of(new HearingTypesList(asList(getHearingType())));
    }

    private HearingType getHearingType() {
        return new HearingType.Builder()
                    .withId(HEARING_TYPE_ID)
                    .withHearingCode("FHG")
                    .withHearingDescription("FIRST HEARING")
                    .withExhibitHearingCode("PTP")
                    .withExhibitHearingDescription("Plea & Trial Preparation")
                    .build();
    }

    private void courtMappingsListTestData() {
        crownCourtMappingsList = Optional.of(new CourtMappingsList(asList(getCourtMapping("CROWN_COURT"))));
        magsCourtMappingsList = Optional.of(new CourtMappingsList(asList(getCourtMapping("MAGISTRATES_COURT"))));
    }

    private CourtMapping getCourtMapping(final String courtType) {
        final String courtSiteId = "433";
        final String crestCourtName = "BLACKFRIARS";
        final String courtSiteName = "BLACKFRIARS";
        final String courtShortName = "BLF";
        final String courtSiteCode = "B";

        return new CourtMapping.Builder()
                .withOucode(OU_CODE)
                .withCrestCourtId(COURT_ID)
                .withCrestCourtSiteId(courtSiteId)
                .withCrestCourtName(crestCourtName)
                .withCrestCourtSiteName(courtSiteName)
                .withCrestCourtShortName(courtShortName)
                .withCrestCourtSiteCode(courtSiteCode)
                .withCourtType(courtType)
                .build();
    }

    private JsonObject getPayloadForCourtRooms(String id) {
        return Json.createObjectBuilder()
                .add("id", id)
                .add("oucode", "B01LY00")
                .add("oucodeL3Name", "South Western (Lavender Hill)")
                .add("oucodeL3WelshName", "welshName_Test")
                .add("courtrooms", getCourtRooms())
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