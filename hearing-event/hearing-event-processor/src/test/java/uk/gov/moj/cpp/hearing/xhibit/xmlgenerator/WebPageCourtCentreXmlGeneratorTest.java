package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.time.ZonedDateTime.parse;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Currentstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Event;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.ObjectFactory;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;
import uk.gov.moj.cpp.hearing.xhibit.XmlUtils;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WebPageCourtCentreXmlGeneratorTest {

    private static final String WEB_PAGE_FILE_PATH = "xhibit/expectedWebPage.xml";
    private static final String WEB_PAGE_FOR_SUMMER_TIME_FILE_PATH = "xhibit/expectedWebPageForSummerTime.xml";

    @Spy
    private XmlUtils xmlUtils;

    @Mock
    private EventGenerator eventGenerator;

    @InjectMocks
    private WebPageCourtCentreXmlGenerator webPageCourtCentreXmlGenerator;

    @Test
    public void shouldCreatePublicDisplayCourtCentreXml() throws IOException {
        final ZonedDateTime lastUpdatedTime = parse("2019-12-05T13:50:00Z");

        final HearingEvent hearingEvent = HearingEvent.hearingEvent()
                .withId(UUID.randomUUID())
                .withHearingId(UUID.randomUUID())
                .withEventTime(lastUpdatedTime)
                .withLastModifiedTime(lastUpdatedTime)
                .build();

        final Currentstatus currentstatus = getCurrentStatus();

        final Optional<CurrentCourtStatus> currentCourtStatus = of(getCurrentCourtStatus(hearingEvent));

        when(eventGenerator.generate(currentCourtStatus.get().getCourt().getCourtSites().get(0).getCourtRooms().get(0))).thenReturn(currentstatus);

        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(PUBLIC_DISPLAY, currentCourtStatus, lastUpdatedTime);

        final String generatedWebPageXml = webPageCourtCentreXmlGenerator.generateXml(courtCentreGeneratorParameters);

        assertXmlEquals(generatedWebPageXml, WEB_PAGE_FILE_PATH);
    }

    @Test
    public void shouldCreatePublicDisplayCourtCentreXmlForSummerTime() throws IOException {
        final ZonedDateTime lastUpdatedTime = parse("2020-03-30T13:50:00Z");
        final HearingEvent hearingEvent = HearingEvent.hearingEvent()
                .withId(UUID.randomUUID())
                .withHearingId(UUID.randomUUID())
                .withEventTime(lastUpdatedTime)
                .withLastModifiedTime(lastUpdatedTime)
                .build();
        final Currentstatus currentstatus = getCurrentStatus();

        final Optional<CurrentCourtStatus> currentCourtStatus = of(getCurrentCourtStatus(hearingEvent));

        when(eventGenerator.generate(currentCourtStatus.get().getCourt().getCourtSites().get(0).getCourtRooms().get(0))).thenReturn(currentstatus);

        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(PUBLIC_DISPLAY, currentCourtStatus, lastUpdatedTime);

        final String generatedWebPageXml = webPageCourtCentreXmlGenerator.generateXml(courtCentreGeneratorParameters);

        assertXmlEquals(generatedWebPageXml, WEB_PAGE_FOR_SUMMER_TIME_FILE_PATH);
    }

    private Currentstatus getCurrentStatus() {
        final ObjectFactory objectFactory = new ObjectFactory();
        final Currentstatus currentstatus = objectFactory.createCurrentstatus();
        final Event event = objectFactory.createEvent();

        event.setTime("12:30");
        event.setDate("12/07/19");
        event.setFreeText(EMPTY);
        event.setType("102000");

        currentstatus.setEvent(event);
        return currentstatus;
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