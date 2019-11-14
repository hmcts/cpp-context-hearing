package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.COURT_LIST_PRODUCED;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.COURT_LIST_REQUESTED;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_FAILED;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_SUCCESSFUL;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportFailed;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportSuccessful;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListProduced;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListRequested;
import uk.gov.moj.cpp.hearing.repository.CourtList;
import uk.gov.moj.cpp.hearing.repository.CourtListPK;
import uk.gov.moj.cpp.hearing.repository.CourtListRepository;

import javax.inject.Inject;

@ServiceComponent(EVENT_LISTENER)
public class PublishCourtListEventListener {

    @Inject
    private CourtListRepository courtListRepository;

    @Handles("hearing.event.publish-court-list-requested")
    public void courtListPublishRequested(final Envelope<PublishCourtListRequested> event) {
        final PublishCourtListRequested publishCourtListRequested = event.payload();
        final CourtListPK courtListPK = new CourtListPK(publishCourtListRequested.getCourtCentreId(), COURT_LIST_REQUESTED);
        courtListRepository.save(new CourtList(courtListPK, publishCourtListRequested.getCreatedTime()));
    }

    @Handles("hearing.event.publish-court-list-produced")
    public void courtListPublishProduced(final Envelope<PublishCourtListProduced> event) {
        final PublishCourtListProduced publishCourtListProduced = event.payload();
        final CourtListPK courtListPK = new CourtListPK(publishCourtListProduced.getCourtCentreId(), COURT_LIST_PRODUCED);
        courtListRepository.save(new CourtList(courtListPK, publishCourtListProduced.getCourtListFileId(),
                publishCourtListProduced.getCourtListFileName(), publishCourtListProduced.getCreatedTime()));
    }

    @Handles("hearing.event.publish-court-list-export-failed")
    public void courtListPublishExportFailed(final Envelope<PublishCourtListExportFailed> event) {
        final PublishCourtListExportFailed publishCourtListExportFailed = event.payload();
        final CourtListPK courtListPK = new CourtListPK(publishCourtListExportFailed.getCourtCentreId(), EXPORT_FAILED);

        final CourtList courtList = new CourtList(courtListPK,
                publishCourtListExportFailed.getCourtListFileId(), publishCourtListExportFailed.getCourtListFileName(),
                publishCourtListExportFailed.getCreatedTime());
        courtList.setErrorMessage(publishCourtListExportFailed.getErrorMessage());
        courtListRepository.save(courtList);
    }

    @Handles("hearing.event.publish-court-list-export-successful")
    public void courtListPublishExportSuccessful(final Envelope<PublishCourtListExportSuccessful> event) {
        final PublishCourtListExportSuccessful publishCourtListExportSuccessful = event.payload();
        final CourtListPK courtListPK = new CourtListPK(publishCourtListExportSuccessful.getCourtCentreId(), EXPORT_SUCCESSFUL);
        courtListRepository.save(new CourtList(courtListPK, publishCourtListExportSuccessful.getCourtListFileId(),
                publishCourtListExportSuccessful.getCourtListFileName(), publishCourtListExportSuccessful.getCreatedTime()));
    }
}