package uk.gov.moj.cpp.hearing.xhibit;

import static java.time.ZonedDateTime.parse;
import static org.apache.commons.io.IOUtils.toInputStream;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.moj.cpp.hearing.XmlProducerType.PUBLIC_DISPLAY;
import static uk.gov.moj.cpp.hearing.XmlProducerType.WEB_PAGE;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.xhibit.pojo.PublishCourtListRequestParameters;
import uk.gov.moj.cpp.hearing.xhibit.xmlgenerator.CourtCentreXmlGenerator;
import uk.gov.moj.cpp.hearing.xhibit.xmlgenerator.CourtCentreXmlGeneratorProducer;
import uk.gov.moj.cpp.listing.common.xhibit.ExportFailedException;
import uk.gov.moj.cpp.listing.common.xhibit.XhibitService;

import java.time.ZonedDateTime;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1854", "squid:S1481","squid:S3457", "squid:S2221", "squid:CommentedOutCodeLine"})
@ServiceComponent(EVENT_PROCESSOR)
public class CourtCentreHearingEventProcessor  {

    private static final Logger LOGGER = LoggerFactory.getLogger(CourtCentreHearingEventProcessor.class.getName());

    @Inject
    private PublishCourtListCommandSender publishCourtListCommandSender;

    @Inject
    private CourtListTimeUpdateRetriever courtListTimeUpdateRetriever;

    @Inject
    private PublishCourtListRequestParametersParser publishCourtListRequestParametersParser;

    @Inject
    private CourtCentreHearingsRetriever courtCentreHearingsRetriever;

    @Inject
    private CourtCentreXmlGeneratorProducer courtCentreXmlGeneratorProducer;

    @Inject
    private XhibitService xhibitService;

    @Inject
    private XhibitFileNameGenerator xhibitFileNameGenerator;


    @Handles("hearing.event.publish-court-list-requested")
    public void handlePublishCourtListRequested(final JsonEnvelope envelope) {

        final PublishCourtListRequestParameters publishCourtListRequestParameters = publishCourtListRequestParametersParser.parse(envelope);
        try {
            final ZonedDateTime latestCourtListUploadTime = courtListTimeUpdateRetriever.getLatestCourtListUploadTime(envelope, publishCourtListRequestParameters.getCourtCentreId());
            processHearingForXhibitWebPage(envelope, publishCourtListRequestParameters, latestCourtListUploadTime);
            processHearingForXhibitPublicDisplay(envelope, publishCourtListRequestParameters, latestCourtListUploadTime);
        } catch (final Exception e) {
            LOGGER.error("Court List generation failed", e);
            publishCourtListCommandSender.recordCourtListExportFailed(publishCourtListRequestParameters.getCourtCentreId(), "NONE", e.getMessage());
        }
    }

    private void processHearingForXhibitWebPage(final JsonEnvelope envelope, final PublishCourtListRequestParameters publishCourtListRequestParameters, final ZonedDateTime latestCourtListUploadTime) throws ExportFailedException {
        final Optional<CurrentCourtStatus> hearingData = courtCentreHearingsRetriever.getHearingDataForWebPage(publishCourtListRequestParameters.getCourtCentreId(), latestCourtListUploadTime, envelope);

        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(WEB_PAGE, hearingData, latestCourtListUploadTime);
        final CourtCentreXmlGenerator courtCentreXmlGenerator = courtCentreXmlGeneratorProducer.getCourtCentreXmlGenerator(courtCentreGeneratorParameters);

        final String xhibitXml = courtCentreXmlGenerator.generateXml(courtCentreGeneratorParameters);

        final ZonedDateTime createdTime = parse(publishCourtListRequestParameters.getCreatedTime());


        final String webPageFileName = xhibitFileNameGenerator.generateWebPageFileName(createdTime, publishCourtListRequestParameters.getCourtCentreId());

        xhibitService.sendToXhibit(toInputStream(xhibitXml), webPageFileName);

        publishCourtListCommandSender.recordCourtListExportSuccessful(publishCourtListRequestParameters.getCourtCentreId(), webPageFileName);
    }

    @SuppressWarnings("squid:UnusedPrivateMethod")
    private void processHearingForXhibitPublicDisplay(final JsonEnvelope envelope, final PublishCourtListRequestParameters publishCourtListRequestParameters, final ZonedDateTime latestCourtListUploadTime) throws ExportFailedException {
        final Optional<CurrentCourtStatus> hearingData = courtCentreHearingsRetriever.getHearingDataForPublicDisplay(publishCourtListRequestParameters.getCourtCentreId(), latestCourtListUploadTime, envelope);

        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(PUBLIC_DISPLAY, hearingData, latestCourtListUploadTime);
        final CourtCentreXmlGenerator courtCentreXmlGenerator = courtCentreXmlGeneratorProducer.getCourtCentreXmlGenerator(courtCentreGeneratorParameters);

        final String xhibitXml = courtCentreXmlGenerator.generateXml(courtCentreGeneratorParameters);

        final String publicDisplayFileName = xhibitFileNameGenerator.generatePublicDisplayFileName(parse(publishCourtListRequestParameters.getCreatedTime()), publishCourtListRequestParameters.getCourtCentreId());

        xhibitService.sendToXhibit(toInputStream(xhibitXml), publicDisplayFileName);

        publishCourtListCommandSender.recordCourtListExportSuccessful(publishCourtListRequestParameters.getCourtCentreId(), publicDisplayFileName);
    }
}
