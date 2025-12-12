package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Event("hearing.event.hearing-resultline-shared-dates-updated")
public class HearingResultLineSharedDatesUpdated implements Serializable {
    private static final long serialVersionUID = 4452241423873321099L;

    private UUID hearingId;
    private List<ResultLineSharedDateInfo> resultLinesToBeUpdated;

    public HearingResultLineSharedDatesUpdated() {
    }

    public HearingResultLineSharedDatesUpdated(final UUID hearingId, final List<ResultLineSharedDateInfo> resultLinesToBeUpdated) {
        this.hearingId = hearingId;
        this.resultLinesToBeUpdated = resultLinesToBeUpdated;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<ResultLineSharedDateInfo> getResultLinesToBeUpdated() {
        return resultLinesToBeUpdated;
    }
}
