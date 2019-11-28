package uk.gov.moj.cpp.hearing.xhibit;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.xhibit.pojo.PublishCourtListRequestParameters;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: Extend this class as part of SCSL-132
@SuppressWarnings({"squid:S1854","squid:S1481","squid:S2221"})
@ServiceComponent(EVENT_PROCESSOR)
public class CourtListEventProcessor {

    private static final String HEARING_QUERY_GET_HEARINGS_BY_COURT_CENTRE = "hearing.get-hearings-by-court-centre";

    private static final Logger LOGGER = LoggerFactory.getLogger(CourtListEventProcessor.class.getName());
    @Inject
    private PublishCourtListCommandSender publishCourtListCommandSender;

    @Inject
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private CourtListTimeUpdateRetriever courtListTimeUpdateRetriever;

    @Inject
    private PublishCourtListRequestParametersParser publishCourtListRequestParametersParser;


    private static final UUID courtListFileId = randomUUID();

    @Handles("hearing.event.publish-court-list-requested")
    public void handlePublishCourtListRequested(final JsonEnvelope envelope) {

        final PublishCourtListRequestParameters publishCourtListRequestParameters = publishCourtListRequestParametersParser.parse(envelope);
        try {
            final ZonedDateTime latestCourtListUploadTime = courtListTimeUpdateRetriever.getLatestCourtListUploadTime(envelope, publishCourtListRequestParameters.getCourtCentreId());

            final JsonObject queryParameters = createObjectBuilder()
                    .add("courtCentreId", publishCourtListRequestParameters.getCourtCentreId())
                    .add("lastModifiedTime", latestCourtListUploadTime.toString())
                    .build();

            final JsonEnvelope jsonEnvelope = requester.requestAsAdmin(enveloper
                    .withMetadataFrom(envelope, HEARING_QUERY_GET_HEARINGS_BY_COURT_CENTRE)
                    .apply(queryParameters));

            publishCourtListCommandSender.recordCourtListExportSuccessful(publishCourtListRequestParameters.getCourtCentreId(), courtListFileId, "TEST");
        } catch (final Exception e) {
            LOGGER.error("Court List generation failed", e);
            publishCourtListCommandSender.recordCourtListExportFailed(publishCourtListRequestParameters.getCourtCentreId(), courtListFileId, "NONE", e.getMessage());
        }
    }
}
