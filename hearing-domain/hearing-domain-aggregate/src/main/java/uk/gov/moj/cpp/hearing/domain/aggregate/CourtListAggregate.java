
package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.doNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportFailed.publishCourtListExportFailed;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportSuccessful.publishCourtListExportSuccessful;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListRequested.publishCourtListRequested;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.COURT_LIST_REQUESTED;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_FAILED;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_SUCCESSFUL;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportFailed;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportSuccessful;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListRequested;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.stream.Stream;


public class CourtListAggregate implements Aggregate {

    private UUID courtCentreId;

    public Stream<Object> recordCourtListRequested(final UUID courtCentreId,
                                                   final ZonedDateTime createdTime) {
        return apply(Stream.of(publishCourtListRequested()
                .withCourtCentreId(courtCentreId)
                .withPublishStatus(COURT_LIST_REQUESTED)
                .withCreatedTime(createdTime)
                .build()));
    }

    public Stream<Object> recordCourtListExportSuccessful(final UUID courtCentreId,
                                                          final UUID courtListFileId,
                                                          final String courtListFileName,
                                                          final ZonedDateTime createdTime) {
        return apply(Stream.of(publishCourtListExportSuccessful()
                .withCourtCentreId(courtCentreId)
                .withCourtListFileId(courtListFileId)
                .withCourtListFileName(courtListFileName)
                .withPublishStatus(EXPORT_SUCCESSFUL)
                .withCreatedTime(createdTime).build()));
    }

    public Stream<Object> recordCourtListExportFailed(final UUID courtCentreId,
                                                      final UUID courtListFileId,
                                                      final String courtListFileName,
                                                      final ZonedDateTime createdTime,
                                                      final String errorMessage) {
        return apply(Stream.of(publishCourtListExportFailed()
                .withCourtCentreId(courtCentreId)
                .withCourtListFileId(courtListFileId)
                .withCourtListFileName(courtListFileName)
                .withPublishStatus(EXPORT_FAILED)
                .withErrorMessage(errorMessage)
                .withCreatedTime(createdTime).build()));
    }

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(PublishCourtListRequested.class).apply(this::recordCourtListRequested),
                when(PublishCourtListExportSuccessful.class).apply(c -> doNothing()),
                when(PublishCourtListExportFailed.class).apply(c -> doNothing())
        );
    }

    private void recordCourtListRequested(final PublishCourtListRequested publishCourtListRequested) {
        this.courtCentreId = publishCourtListRequested.getCourtCentreId();
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }
}