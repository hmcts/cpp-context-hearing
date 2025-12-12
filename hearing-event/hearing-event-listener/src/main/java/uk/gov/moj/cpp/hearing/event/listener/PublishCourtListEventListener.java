package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.COURT_LIST_REQUESTED;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_FAILED;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_SUCCESSFUL;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportFailed;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportSuccessful;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListRequested;
import uk.gov.moj.cpp.hearing.repository.CourtListPublishStatus;
import uk.gov.moj.cpp.hearing.repository.CourtListRepository;

import javax.inject.Inject;

@ServiceComponent(EVENT_LISTENER)
public class PublishCourtListEventListener {

    @Inject
    private CourtListRepository courtListRepository;

    @Handles("hearing.event.publish-court-list-requested")
    public void courtListPublishRequested(final Envelope<PublishCourtListRequested> event) {
        final PublishCourtListRequested publishCourtListRequested = event.payload();
        final CourtListPublishStatus publishRequested = new CourtListPublishStatus(randomUUID(), publishCourtListRequested.getCourtCentreId(),
                 COURT_LIST_REQUESTED, publishCourtListRequested.getCreatedTime());
        courtListRepository.save(publishRequested);
    }

    @Handles("hearing.event.publish-court-list-export-failed")
    public void courtListPublishExportFailed(final Envelope<PublishCourtListExportFailed> event) {
        final PublishCourtListExportFailed publishCourtListExportFailed = event.payload();

        final CourtListPublishStatus exportFailed = new CourtListPublishStatus(
                randomUUID(), publishCourtListExportFailed.getCourtCentreId(),
                 EXPORT_FAILED, publishCourtListExportFailed.getCreatedTime()
                , publishCourtListExportFailed.getCourtListFileName(), publishCourtListExportFailed.getErrorMessage()
        );
        exportFailed.setErrorMessage(publishCourtListExportFailed.getErrorMessage());
        courtListRepository.save(exportFailed);
    }

    @Handles("hearing.event.publish-court-list-export-successful")
    public void courtListPublishExportSuccessful(final Envelope<PublishCourtListExportSuccessful> event) {
        final PublishCourtListExportSuccessful publishCourtListExportSuccessful = event.payload();
        final CourtListPublishStatus exportSuccessful = new CourtListPublishStatus(
                randomUUID(), publishCourtListExportSuccessful.getCourtCentreId(),
                EXPORT_SUCCESSFUL, publishCourtListExportSuccessful.getCreatedTime(),
                 publishCourtListExportSuccessful.getCourtListFileName(), ""
        );
        courtListRepository.save(exportSuccessful);
    }
}