package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.time.ZonedDateTime.parse;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
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
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Currentstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Event;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory;
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

    @Spy
    private XmlUtils xmlUtils;

    @Mock
    private PublicDisplayEventGenerator publicDisplayEventGenerator;

    @InjectMocks
    private PublicDisplayCourtCentreXmlGenerator publicDisplayCourtCentreXmlGenerator;

    @Test
    public void shouldCreatePublicDisplayCourtCentreXml() throws IOException {

        final HearingEvent hearingEvent = mock(HearingEvent.class);
        final Currentstatus currentstatus = getCurrentStatus();

        final Optional<CurrentCourtStatus> currentCourtStatus = of(getCurrentCourtStatus(hearingEvent));

        final ZonedDateTime lastUpdatedTime = parse("2019-12-05T13:50:00Z");

        when(publicDisplayEventGenerator.generate(currentCourtStatus.get().getCourt().getCourtSites().get(0).getCourtRooms().get(0).getCases().getCasesDetails().get(0))).thenReturn(currentstatus);

        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(PUBLIC_DISPLAY, currentCourtStatus, lastUpdatedTime);

        final String generatedPublicPageXml = publicDisplayCourtCentreXmlGenerator.generateXml(courtCentreGeneratorParameters);
        System.out.println(generatedPublicPageXml);
        assertXmlEquals(generatedPublicPageXml, PUBLIC_PAGE_FILE_PATH);
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