package uk.gov.moj.cpp.hearing.xhibit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.XmlProducerType.WEB_PAGE;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Court;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.xhibit.pojo.PublishCourtListRequestParameters;
import uk.gov.moj.cpp.hearing.xhibit.xmlgenerator.CourtCentreXmlGenerator;
import uk.gov.moj.cpp.hearing.xhibit.xmlgenerator.CourtCentreXmlGeneratorProducer;
import uk.gov.moj.cpp.listing.common.xhibit.XhibitService;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CourtCentreHearingEventProcessorTest {

    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private CourtCentreXmlGenerator courtCentreXmlGenerator;

    @Mock
    private PublishCourtListCommandSender publishCourtListCommandSender;

    @Mock
    private CourtListTimeUpdateRetriever courtListTimeUpdateRetriever;

    @Mock
    private PublishCourtListRequestParametersParser publishCourtListRequestParametersParser;

    @Mock
    private CourtCentreHearingsRetriever courtCentreHearingsRetriever;

    @Mock
    private CourtCentreXmlGeneratorProducer courtCentreXmlGeneratorProducer;

    @Mock
    private XhibitService xhibitService;

    @Mock
    private XhibitFileNameGenerator xhibitFileNameGenerator;

    @InjectMocks
    private CourtCentreHearingEventProcessor courtCentreHearingEventProcessor;

    @Test
    public void shouldHandlePublishCourtListRequested() {

        final String courtCentreId = UUID.randomUUID().toString();
        final String createdTime = LocalDateTime.now().toString();
        final ZonedDateTime latestCourtListUploadTime = ZonedDateTime.now();

        final PublishCourtListRequestParameters parameters = new PublishCourtListRequestParameters(courtCentreId, latestCourtListUploadTime.toString());
        final Court court = Court.court().withCourtName("courtName").build();
        final String xhibitXml = "xhibitXml";
        final String webPageFileName = "webPageFileName";
        final String publicDisplayFileName = "publicDisplayFileName";
        Optional<CurrentCourtStatus> currentCourtStatus = Optional.of(CurrentCourtStatus.currentCourtStatus().withCourt(court).withPageName(webPageFileName).build());

        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(WEB_PAGE, currentCourtStatus, latestCourtListUploadTime);
        when(publishCourtListRequestParametersParser.parse(jsonEnvelope))
                .thenReturn(parameters);
        when(courtListTimeUpdateRetriever.getLatestCourtListUploadTime(jsonEnvelope, courtCentreId))
                .thenReturn(latestCourtListUploadTime);
        when(courtCentreXmlGeneratorProducer.getCourtCentreXmlGenerator(any(CourtCentreGeneratorParameters.class)))
                .thenReturn(courtCentreXmlGenerator);
        when(courtCentreHearingsRetriever.getHearingDataForWebPage(courtCentreId, latestCourtListUploadTime, jsonEnvelope))
                .thenReturn(currentCourtStatus);
        when(courtCentreXmlGenerator.generateXml(any(CourtCentreGeneratorParameters.class)))
                .thenReturn(xhibitXml);
        when(xhibitFileNameGenerator.generateWebPageFileName(latestCourtListUploadTime, courtCentreId))
                .thenReturn(webPageFileName);
        when(xhibitFileNameGenerator.generatePublicDisplayFileName(latestCourtListUploadTime, courtCentreId))
                .thenReturn(publicDisplayFileName);

        courtCentreHearingEventProcessor.handlePublishCourtListRequested(jsonEnvelope);
        verify(publishCourtListCommandSender).recordCourtListExportSuccessful(parameters.getCourtCentreId(), webPageFileName);

        verify(publishCourtListCommandSender).recordCourtListExportSuccessful(parameters.getCourtCentreId(), publicDisplayFileName);

    }
}