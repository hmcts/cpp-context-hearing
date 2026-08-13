package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.listing.common.utils.FileUtil.givenPayload;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.listing.common.xhibit.exception.InvalidReferenceDataException;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtMapping;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtRoomMapping;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtRoomMappingsList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.HearingType;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.Judiciary;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.OrganisationUnit;
import uk.gov.moj.cpp.hearing.listing.domain.xhibit.CourtLocation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class CommonXhibitReferenceDataServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonXhibitReferenceDataServiceTest.class);

    @Mock(answer = RETURNS_DEEP_STUBS)
    private ReferenceDataCache referenceDataCache;

    @InjectMocks
    private CommonXhibitReferenceDataService commonXhibitReferenceDataService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;


    @BeforeEach
    public void init() {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.jsonObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldGetCrownCourtDetails() {

        final UUID courtCentreId = randomUUID();
        final String ouCode = "OUCODE";
        final String courtId = "432";
        final String courtSiteId = "433";
        final String crestCourtName = "BLACKFRIARS";
        final String courtSiteName = "BLACKFRIARS";
        final String courtShortName = "BLF";
        final String courtSiteCode = "B";
        final String courtType = "CROWN_COURT";

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

        when(referenceDataCache.getCrownCourtMappingsMapCache(courtCentreId)).thenReturn(Optional.of(Arrays.asList(courtMapping)));

        final CourtLocation courtDetails = commonXhibitReferenceDataService.getCrownCourtDetails(courtCentreId);

        assertEquals(courtDetails.getOuCode(), ouCode);
        assertEquals(courtDetails.getCrestCourtId(), courtId);
        assertEquals(courtDetails.getCrestCourtSiteId(), courtSiteId);
        assertEquals(courtDetails.getCourtSiteName(), courtSiteName);
        assertEquals(courtDetails.getCourtShortName(), courtShortName);
        assertEquals(courtDetails.getCourtSiteCode(), courtSiteCode);
        assertEquals(courtDetails.getCourtType(), courtType);
    }

    @Test
    public void shouldGetMagsCourtDetails() {

        final UUID courtCentreId = randomUUID();
        final String ouCode = "OUCODE";
        final String courtId = "432";
        final String courtSiteId = "433";
        final String crestCourtName = "BLACKFRIARS";
        final String courtSiteName = "BLACKFRIARS";
        final String courtShortName = "BLF";
        final String courtSiteCode = "B";
        final String courtType = "MAGISTRATES_COURT";

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

        when(referenceDataCache.getMagsCourtMappingsMapCache(courtCentreId)).thenReturn(Optional.of(Arrays.asList(courtMapping)));

        final CourtLocation courtDetails = commonXhibitReferenceDataService.getMagsCourtDetails(courtCentreId);

        assertEquals(courtDetails.getOuCode(), ouCode);
        assertEquals(courtDetails.getCrestCourtId(), courtId);
        assertEquals(courtDetails.getCrestCourtSiteId(), courtSiteId);
        assertEquals(courtDetails.getCourtSiteName(), courtSiteName);
        assertEquals(courtDetails.getCourtShortName(), courtShortName);
        assertEquals(courtDetails.getCourtSiteCode(), courtSiteCode);
        assertEquals(courtDetails.getCourtType(), courtType);
    }

    @Test
    public void shouldGetJudiciary() {
        final String titleSuffix = "judge";
        final String titlePrefix = "Mr";
        final String titleJudicialPrefix = "Recorder";
        final String surname = "Ainsworth";
        final String forenames = "Mark J";

        final UUID judiciaryId = randomUUID();

        final Judiciary judiciary = new Judiciary.Builder()
                .withId(judiciaryId)
                .withTitlePrefix(titlePrefix)
                .withTitleJudicialPrefix(titleJudicialPrefix)
                .withTitleSuffix(titleSuffix)
                .withSurname(surname)
                .withForenames(forenames)
                .build();

        when(referenceDataCache.getJudiciariesMapCache(judiciaryId)).thenReturn(of(judiciary));

        final JsonObject judiciaryJsonObject = commonXhibitReferenceDataService.getJudiciary(judiciaryId);

        assertThat(judiciaryJsonObject.getString("titlePrefix"), is(titlePrefix));
        assertThat(judiciaryJsonObject.getString("titleJudicialPrefix"), is(titleJudicialPrefix));
        assertThat(judiciaryJsonObject.getString("titleSuffix"), is(titleSuffix));
        assertThat(judiciaryJsonObject.getString("surname"), is(surname));
        assertThat(judiciaryJsonObject.getString("forenames"), is(forenames));

    }

    @Test
    public void shouldGetCourtRoomNumberForMappedCourtRoom() {
        final int expectedCourtRoomNumber = 777;
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = fromString("b3c9eb70-93eb-4570-a1fa-7516a5e4e9cd");

        final JsonObject courtRoomMappingJson = givenPayload("/mock-data/referencedata.query.cp-xhibit-courtroom-mappings.json");
        final CourtRoomMappingsList courtRoomMappingsList = jsonObjectConverter.convert(courtRoomMappingJson, CourtRoomMappingsList.class);

        when(referenceDataCache.getCourtRoomMappingByCourtCentreAndCourtRoom(courtCentreId, courtRoomId))
                .thenReturn(Optional.of(courtRoomMappingsList.getCpXhibitCourtRoomMappings().get(0)));

        int actualCourtRoomNumber = commonXhibitReferenceDataService.getCourtRoomNumber(courtCentreId, courtRoomId);

        assertEquals(actualCourtRoomNumber, expectedCourtRoomNumber);
    }

    @Test
    public void shouldGetCourtRoomNumberForUnmappedCourtRoom() {
        final int expectedCourtRoomNumber = -99;
        final UUID courtCentreId = randomUUID();
        final UUID wrongCourtRoomId = fromString("b3c9eb70-93eb-4570-a1fa-7516a5e4e9ce");

        final JsonObject courtRoomMappingJson = givenPayload("/mock-data/referencedata.query.cp-xhibit-courtroom-mappings.json");
        final CourtRoomMappingsList courtRoomMappingsList = jsonObjectConverter.convert(courtRoomMappingJson, CourtRoomMappingsList.class);

        when(referenceDataCache.getCourtRoomMappingByCourtCentreAndCourtRoom(courtCentreId, wrongCourtRoomId)).thenReturn(empty());

        int actualCourtRoomNumber = commonXhibitReferenceDataService.getCourtRoomNumber(courtCentreId, wrongCourtRoomId);

        assertEquals(actualCourtRoomNumber, expectedCourtRoomNumber);
    }

    @Test
    public void shouldGetXhibitHearingType() {
        final UUID cppHearingTypeId = randomUUID();

        final HearingType hearingType = new HearingType.Builder()
                .withId(cppHearingTypeId)
                .build();

        when(referenceDataCache.getHearingTypeCache(cppHearingTypeId)).thenReturn(of(hearingType));

        final HearingType xhibitHearingType = commonXhibitReferenceDataService.getXhibitHearingType(cppHearingTypeId);
        LOGGER.info("xhibitHearingType = " + xhibitHearingType);

        assertThat(xhibitHearingType.getId(), equalTo(cppHearingTypeId));
    }

    @Test
    public void shouldGetCourtCentreIdsForCrestId() {
        final String crownCourtCrestId = "CRESTID";
        final String crestCourtSiteId = "426";
        final UUID ouId = randomUUID();
        final String ouCode = "C01BL00";

        final CourtMapping courtMapping = new CourtMapping.Builder()
                .withCrestCourtId(crownCourtCrestId)
                .withCrestCourtSiteId(crestCourtSiteId)
                .withOucode(ouCode)
                .build();

        final OrganisationUnit organisationUnit = new OrganisationUnit.Builder()
                .withId(ouId)
                .withOucode(ouCode)
                .build();

        when(referenceDataCache.getOrganisationUnitMapCache(ouCode)).thenReturn(Optional.of(organisationUnit));
        when(referenceDataCache.getCrownCourtLocationsCache(crownCourtCrestId))
                .thenReturn(Arrays.asList(courtMapping).stream().map(this::createCourtLocation).collect(Collectors.toList()));

        final List<UUID> courtCentreIds = commonXhibitReferenceDataService.getCrownCourtCentreIdsForCrestId(crownCourtCrestId);

        assertThat(courtCentreIds.size(), is(equalTo(1)));
        assertThat(courtCentreIds.get(0), is(Matchers.equalTo(ouId)));
    }

    @Test
    public void shouldThrowInvalidReferenceDataExceptionWhenGetCourtCentreIdsForCrestId() {
        final String crownCourtCrestId = "CRESTID";
        final String crestCourtSiteId = EMPTY;
        final UUID ouId = randomUUID();
        final String ouCode = "C01BL00";

        final CourtMapping courtMapping = new CourtMapping.Builder()
                .withCrestCourtId(crownCourtCrestId)
                .withCrestCourtSiteId(crestCourtSiteId)
                .withOucode(ouCode)
                .build();

        when(referenceDataCache.getCrownCourtLocationsCache(crownCourtCrestId))
                .thenReturn(Stream.of(courtMapping).map(this::createCourtLocation).collect(Collectors.toList()));

        assertThrows(InvalidReferenceDataException.class, () -> commonXhibitReferenceDataService.getCrownCourtCentreIdsForCrestId(crownCourtCrestId));
    }

    @Test
    public void shouldGetCourtCentreIdsWhenCallToGetMagsCourtCentreIdsForCrestId() {
        final UUID magsCourtCrestId = randomUUID();
        final String crestCourtSiteId = "426";
        final UUID ouId = randomUUID();
        final String ouCode = "C01BL00";

        final CourtMapping courtMapping = new CourtMapping.Builder()
                .withCrestCourtId(magsCourtCrestId.toString())
                .withCrestCourtSiteId(crestCourtSiteId)
                .withOucode(ouCode)
                .build();

        final OrganisationUnit organisationUnit = new OrganisationUnit.Builder()
                .withId(ouId)
                .withOucode(ouCode)
                .build();

        when(referenceDataCache.getOrganisationUnitMapCache(ouCode)).thenReturn(Optional.of(organisationUnit));
        when(referenceDataCache.getMagsCourtMappingsMapCache(magsCourtCrestId))
                .thenReturn(Optional.of(Arrays.asList(courtMapping)));

        final List<UUID> courtCentreIds = commonXhibitReferenceDataService.getMagsCourtCentreIdsForCrestId(magsCourtCrestId.toString());

        assertThat(courtCentreIds.size(), is(equalTo(1)));
        assertThat(courtCentreIds.get(0), is(Matchers.equalTo(ouId)));
    }

    // Warning - this suppression of warnings is solely to get sonar to pass so the junit5
    // upgrade can be merged. The @SuppressWarnings needs to be removed and the test refactored
    @SuppressWarnings("java:S5778")
    @Test
    public void shouldThrowInValidReferenceDataExceptionWhenCallToGetMagsCourtCentreIdsForCrestId() {
        final UUID magsCourtCrestId = randomUUID();
        final String crestCourtSiteId = "426";
        final String ouCode = "C01BL00";

        final CourtMapping courtMapping = new CourtMapping.Builder()
                .withCrestCourtId(magsCourtCrestId.toString())
                .withCrestCourtSiteId(crestCourtSiteId)
                .withOucode(ouCode)
                .build();

        when(referenceDataCache.getOrganisationUnitMapCache(ouCode)).thenReturn(Optional.empty());
        when(referenceDataCache.getMagsCourtMappingsMapCache(magsCourtCrestId))
                .thenReturn(Optional.of(Arrays.asList(courtMapping)));

        assertThrows(InvalidReferenceDataException.class, () -> commonXhibitReferenceDataService.getMagsCourtCentreIdsForCrestId(magsCourtCrestId.toString()));

    }

    @Test
    public void shouldGetCrestCourtSitesForCourtCentre() {
        final UUID courtCentreId = randomUUID();
        final String ouCode = "C01BL00";

        final String courtId = "432";
        final String courtSiteId = "433";
        final String crestCourtName = "BLACKFRIARS";
        final String courtSiteName = "BLACKFRIARS";
        final String courtShortName = "BLF";
        final String courtSiteCode = "B";
        final String courtType = "MAGISTRATES_COURT";

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

        when(referenceDataCache.getCrownCourtMappingsMapCache(courtCentreId)).thenReturn(Optional.of(Arrays.asList(courtMapping)));

        final List<JsonObject> crestCourtSites = commonXhibitReferenceDataService.getCrestCourtSitesForCrownCourtCentre(courtCentreId);

        assertThat(crestCourtSites.size(), is(equalTo(1)));
        assertThat(crestCourtSites.get(0).getString("oucode"), is(ouCode));
        assertThat(crestCourtSites.get(0).getString("crestCourtId"), is(courtId));
        assertThat(crestCourtSites.get(0).getString("crestCourtSiteId"), is(courtSiteId));
        assertThat(crestCourtSites.get(0).getString("crestCourtSiteName"), is(courtSiteName));
        assertThat(crestCourtSites.get(0).getString("crestCourtName"), is(crestCourtName));
        assertThat(crestCourtSites.get(0).getString("crestCourtSiteCode"), is(courtSiteCode));
        assertThat(crestCourtSites.get(0).getString("courtType"), is(courtType));
    }

    @Test
    public void shouldGetDefaultCrestCourtSitesCode() {
        final UUID courtCentreId = randomUUID();
        final String ouCode = "C01BL00";

        final String courtId = "432";
        final String courtSiteId = "433";
        final String crestCourtName = "BLACKFRIARS";
        final String courtSiteName = "BLACKFRIARS";
        final String courtShortName = "BLF";
        final String courtSiteCode = "B";
        final String courtType = "MAGISTRATES_COURT";

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

        when(referenceDataCache.getCrownCourtMappingsMapCache(courtCentreId)).thenReturn(Optional.of(Arrays.asList(courtMapping)));

        final String defaultCrestCourtSiteCode = commonXhibitReferenceDataService.getDefaultCrestCourtSiteCode(courtCentreId);

        assertThat(defaultCrestCourtSiteCode, is(equalTo("B")));

    }



    @Test
    public void shouldGetCourtRoom() {
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();

        final JsonObject courtRoomMappingJson = givenPayload("/mock-data/referencedata.query.cp-xhibit-courtroom-mappings.json");
        final CourtRoomMappingsList courtRoomMappingsList = jsonObjectConverter.convert(courtRoomMappingJson, CourtRoomMappingsList.class);
        final JsonObject cpCourtRoom = Json.createObjectBuilder()
                .add("id", courtRoomId.toString())
                .add("courtroomId", 1234)
                .build();

        when(referenceDataCache.getCpCourtRoomCache(courtCentreId)).thenReturn(asList(cpCourtRoom));
        when(referenceDataCache.getCourtRoomMappingsMapCache(courtCentreId)).thenReturn(courtRoomMappingsList);

        final Optional<CourtRoomMapping> courtRoom = commonXhibitReferenceDataService.getCourtRoom(courtCentreId, courtRoomId);

        assertThat(courtRoom.isPresent(), is(true));
        assertThat(courtRoom.get().getCourtRoomId(), is(1234));

    }


    private CourtLocation createCourtLocation(final CourtMapping courtMapping) {

        return new CourtLocation(
                courtMapping.getOucode(),
                courtMapping.getCrestCourtId(),
                courtMapping.getCrestCourtSiteId(),
                courtMapping.getCrestCourtName(),
                courtMapping.getCrestCourtShortName(),
                courtMapping.getCrestCourtSiteName(),
                courtMapping.getCrestCourtSiteCode(),
                courtMapping.getCourtType());
    }
}