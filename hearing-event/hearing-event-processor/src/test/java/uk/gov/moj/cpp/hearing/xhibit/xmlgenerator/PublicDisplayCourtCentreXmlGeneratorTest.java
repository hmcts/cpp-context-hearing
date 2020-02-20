package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.math.BigInteger.valueOf;
import static java.time.ZonedDateTime.parse;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static uk.gov.moj.cpp.hearing.XmlProducerType.PUBLIC_DISPLAY;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail.caseDetail;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Cases.cases;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Court.court;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom.courtRoom;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtSite.courtSite;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus.currentCourtStatus;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Defendant.defendant;
import static uk.gov.moj.cpp.hearing.xhibit.XmlTestUtils.assertXmlEquals;

import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;
import uk.gov.moj.cpp.hearing.xhibit.XmlUtils;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublicDisplayCourtCentreXmlGeneratorTest {
    private static final String PUBLIC_PAGE_FILE_PATH = "xhibit/expectedPublicPage.xml";
    private static final String PUBLIC_PAGE_FOR_ACTIVE_CASES_FILE_PATH = "xhibit/expectedPublicPageForActiveCases.xml";

    @Spy
    private XmlUtils xmlUtils;

    @Mock
    private PublicDisplayEventGenerator publicDisplayEventGenerator;

    @InjectMocks
    private PublicDisplayCourtCentreXmlGenerator publicDisplayCourtCentreXmlGenerator;

    @Test
    public void shouldCreatePublicDisplayCourtCentreXml() throws IOException {

        final HearingEvent hearingEvent = mock(HearingEvent.class);

        final Optional<CurrentCourtStatus> currentCourtStatus = of(getCurrentCourtStatus(hearingEvent));

        final ZonedDateTime lastUpdatedTime = parse("2019-12-05T13:50:00Z");
        final String courtCentreId = randomUUID().toString();
        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(PUBLIC_DISPLAY, currentCourtStatus, lastUpdatedTime);

        final String generatedPublicPageXml = publicDisplayCourtCentreXmlGenerator.generateXml(courtCentreGeneratorParameters);
        assertXmlEquals(generatedPublicPageXml, PUBLIC_PAGE_FILE_PATH);
    }

    @Test
    public void shouldCreatePublicDisplayCourtCentreXmlWithActiveCaseFlagSet() throws IOException {

        final HearingEvent hearingEvent = mock(HearingEvent.class);
        final Optional<CurrentCourtStatus> currentCourtStatus = of(getCurrentCourtStatusWithMultipleCases(hearingEvent));

        final ZonedDateTime lastUpdatedTime = parse("2019-12-05T13:50:00Z");
        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(PUBLIC_DISPLAY, currentCourtStatus, lastUpdatedTime);

        final String generatedPublicPageXml = publicDisplayCourtCentreXmlGenerator.generateXml(courtCentreGeneratorParameters);
        assertXmlEquals(generatedPublicPageXml, PUBLIC_PAGE_FOR_ACTIVE_CASES_FILE_PATH);
    }

    private CurrentCourtStatus getCurrentCourtStatus(final HearingEvent hearingEvent) {
        return currentCourtStatus()
                .withCourt(court()
                        .withCourtName("testCourtName")
                        .withCourtSites(asList(courtSite()
                                .withCourtSiteName("testCourtSiteName")
                                .withCourtRooms(asList(courtRoom()
                                        .withCourtRoomName("courtRoomName")
                                        .withHearingEvent(hearingEvent)
                                        .withCases(cases().withCasesDetails(asList(caseDetail2()))
                                                .build())
                                        .build()))
                                .build()))
                        .build()).build();
    }

    private CurrentCourtStatus getCurrentCourtStatusWithMultipleCases(final HearingEvent hearingEvent) {
        return currentCourtStatus()
                .withCourt(court()
                        .withCourtName("testCourtName")
                        .withCourtSites(asList(courtSite()
                                .withCourtSiteName("testCourtSiteName")
                                .withCourtRooms(asList(courtRoom()
                                        .withCourtRoomName("courtRoomName")
                                        .withHearingEvent(hearingEvent)
                                        .withCases(cases().withCasesDetails(asList(caseDetail2(), caseDetail3()))
                                                .build())
                                        .build()))
                                .build()))
                        .build()).build();
    }

    private CaseDetail caseDetail2() {
        return caseDetail()
                .withActivecase(valueOf(0))
                .withCaseNumber("1")
                .withCaseType("caseType")
                .withCppUrn("234")
                .withHearingType("hearingType")
                .withDefendants(asList(defendant().withFirstName("Alexander").withMiddleName("de").withLastName("Jong").build()))
                .withJudgeName("Mr Lampard")
                .withNotBeforeTime("2020-02-09T15:00Z[UTC]")
                .build();
    }

    private CaseDetail caseDetail3() {
        return caseDetail()
                .withActivecase(valueOf(1))
                .withCaseNumber("1")
                .withCaseType("caseType")
                .withCppUrn("235")
                .withHearingType("hearingType")
                .withDefendants(asList(defendant().withFirstName("Alexander").withMiddleName("de").withLastName("Jong").build()))
                .withJudgeName("Mr Lampard")
                .withNotBeforeTime("2020-02-09T15:00Z[UTC]")
                .build();
    }
}