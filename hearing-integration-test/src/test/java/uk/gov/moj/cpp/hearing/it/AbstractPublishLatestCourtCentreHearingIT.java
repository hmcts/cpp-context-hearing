package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.fromString;
import static uk.gov.moj.cpp.hearing.utils.ProgressionStub.stubGetProgressionProsecutionCases;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataCourtRoomMappings;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataCourtXhibitCourtMappings;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataEventMappings;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataJudiciaries;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataXhibitHearingTypes;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubOrganisationUnit;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.stubExhibitFileUpload;

import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.BeforeClass;

public class AbstractPublishLatestCourtCentreHearingIT extends AbstractIT {

    protected static String courtCentreId;
    protected static String courtCentreId_1;
    protected static String courtCentreId_2;
    protected static String courtCentreId_3;
    protected static String courtCentreId_4;
    protected static String courtRoom1Id;
    protected static String courtRoom2Id;
    protected static String courtRoom3Id;
    protected static String courtRoom4Id;
    protected static String courtRoom5Id;
    protected static String defenceCounselId;
    protected static UUID caseId;
    protected static UUID hearingTypeId;

    @BeforeClass
    public static void setUpBeforeClassAbstractPublishLatestCourtCentreHearingIT() {
        stubGetReferenceDataXhibitHearingTypes();
        stubGetReferenceDataCourtXhibitCourtMappings();
        stubGetReferenceDataEventMappings();
        stubGetReferenceDataJudiciaries();
        stubExhibitFileUpload();

        /** Easier to debug when you have non-random UUIDs */
        hearingTypeId = fromString("9cc41e45-b594-4ba6-906e-1a4626b08fed");
        courtCentreId = "9d9824b3-8a61-472f-8b55-e532ceabb403";
        courtCentreId_1 = "d9c200c1-5d63-405d-8ebb-4d1ac3b37144";
        courtCentreId_2 = "c1006f71-233a-4292-aca0-9e4b66380c36";
        courtCentreId_3 = "52955c69-c749-416d-a3ca-8ca881709325";
        courtCentreId_4 = "b35d8ea1-6839-47a1-9944-4a8515175224";
        courtRoom1Id = "9a0124b3-8a61-472f-8b55-e532ceabb403";
        courtRoom2Id = "5d5d7b5e-7833-4dc1-b94a-d1a5c635c623";
        courtRoom3Id = "b8e8d8e7-1964-4613-b5e7-940feaa7957a";
        courtRoom4Id = "5ea96bd6-0712-4415-ac24-0194a4fa8251";
        courtRoom5Id = "9a0124b3-8a61-472f-8b55-e532ceabb402";
        defenceCounselId = "9dc824b3-8a61-472f-8b55-e532ceabb403";
        caseId = fromString("9c9824b3-8a61-472f-8b55-e532ceabb403");

        stubGetReferenceDataCourtRoomMappings(courtRoom1Id, courtRoom2Id, courtRoom3Id, courtRoom4Id, courtRoom5Id);
        stubOrganisationUnit(courtCentreId, courtCentreId_1, courtCentreId_2, courtCentreId_3, courtCentreId_4, ouId1, ouId2, ouId3, ouId4);

        stubGetProgressionProsecutionCases(caseId);
    }
}
