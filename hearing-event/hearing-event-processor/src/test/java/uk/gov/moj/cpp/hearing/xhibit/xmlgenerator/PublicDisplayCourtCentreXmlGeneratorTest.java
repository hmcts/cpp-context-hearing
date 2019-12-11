package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.time.ZonedDateTime.parse;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static uk.gov.moj.cpp.hearing.XmlProducerType.PUBLIC_DISPLAY;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail.caseDetail;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Cases.cases;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Court.court;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom.courtRoom;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtSite.courtSite;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus.currentCourtStatus;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Defendant.defendant;
import static uk.gov.moj.cpp.hearing.xhibit.XmlTestUtils.assertXmlEquals;

import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;
import uk.gov.moj.cpp.hearing.xhibit.XmlUtils;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublicDisplayCourtCentreXmlGeneratorTest {

    private static final String PUBLIC_DISPLAY_FILE_PATH = "xhibit/expectedPublicDisplay.xml";

    @Spy
    private XmlUtils xmlUtils;

    @InjectMocks
    private PublicDisplayCourtCentreXmlGenerator publicDisplayCourtCentreXmlGenerator;

    @Test
    public void shouldCretePublicDisplayCourtCentreXml() throws IOException {

        final Optional<CurrentCourtStatus> currentCourtStatus = of(getCurrentCourtStatus());

        final ZonedDateTime lastUpdatedTime = parse("2019-12-05T13:50:00Z");

        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(PUBLIC_DISPLAY, currentCourtStatus, lastUpdatedTime);

        final String generatedPublicDisplayXml = publicDisplayCourtCentreXmlGenerator.generateXml(courtCentreGeneratorParameters);

        assertXmlEquals(generatedPublicDisplayXml, PUBLIC_DISPLAY_FILE_PATH);
    }

    private CurrentCourtStatus getCurrentCourtStatus() {
        return currentCourtStatus()
                .withCourt(court()
                        .withCourtName("testCourtName")
                        .withCourtSites(asList(courtSite()
                                .withCourtSiteName("testCourtSiteName")
                                .withCourtRooms(asList(courtRoom()
                                        .withCourtRoomName("courtRoomName")
                                        .withCases(cases()
                                                .withCasesDetails(asList(caseDetail()
                                                        .withCaseNumber("123")
                                                        .withCaseType("caseType")
                                                        .withCppUrn("234")
                                                        .withHearingType("hearingType")
                                                        .withDefendants(asList(defendant().withFirstName("Alexander").withMiddleName("de").withLastName("Jong").build()))
                                                        .withJudgeName("Mr Lampard")
                                                        .build()))
                                                .build())
                                        .build()))
                                .build()))
                        .build()).build();
    }
}