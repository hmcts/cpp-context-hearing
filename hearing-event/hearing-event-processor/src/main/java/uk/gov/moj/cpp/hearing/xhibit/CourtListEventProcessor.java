package uk.gov.moj.cpp.hearing.xhibit;

import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.moj.cpp.hearing.XmlProducerType.PUBLIC_DISPLAY;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.CourtCentreXmlGeneratorProducer;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.xhibit.pojo.PublishCourtListRequestParameters;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: Extend this class as part of SCSL-132
@SuppressWarnings({"squid:S1854", "squid:S1481", "squid:S2221"})
@ServiceComponent(EVENT_PROCESSOR)
public class CourtListEventProcessor {


    private static final Logger LOGGER = LoggerFactory.getLogger(CourtListEventProcessor.class.getName());
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


    private static final UUID courtListFileId = randomUUID();

    @Handles("hearing.event.publish-court-list-requested")
    public void handlePublishCourtListRequested(final JsonEnvelope envelope) {

        final PublishCourtListRequestParameters publishCourtListRequestParameters = publishCourtListRequestParametersParser.parse(envelope);
        try {
            final ZonedDateTime latestCourtListUploadTime = courtListTimeUpdateRetriever.getLatestCourtListUploadTime(envelope, publishCourtListRequestParameters.getCourtCentreId());

            final Optional<CurrentCourtStatus> hearingData = courtCentreHearingsRetriever.getHearingData(publishCourtListRequestParameters.getCourtCentreId(), latestCourtListUploadTime, envelope);

            final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(PUBLIC_DISPLAY, hearingData, latestCourtListUploadTime);
            final CourtCentreXmlGenerator courtCentreXmlGenerator = courtCentreXmlGeneratorProducer.getCourtCentreXmlGenerator(courtCentreGeneratorParameters);

            final String generateXml = courtCentreXmlGenerator.generateXml(courtCentreGeneratorParameters);

            publishCourtListCommandSender.recordCourtListExportSuccessful(publishCourtListRequestParameters.getCourtCentreId(), courtListFileId, "TEST");
        } catch (final Exception e) {
            LOGGER.error("Court List generation failed", e);
            publishCourtListCommandSender.recordCourtListExportFailed(publishCourtListRequestParameters.getCourtCentreId(), courtListFileId, "NONE", e.getMessage());
        }
    }
}
