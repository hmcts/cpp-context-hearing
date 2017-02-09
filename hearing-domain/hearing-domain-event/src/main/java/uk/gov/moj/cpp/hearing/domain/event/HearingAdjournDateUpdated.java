package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.adjourn-date-updated")
public class HearingAdjournDateUpdated {

    private final UUID hearingId;
    private final LocalDate startDate;

    public HearingAdjournDateUpdated(final UUID hearingId, final LocalDate startDate) {
        this.hearingId = hearingId;
        this.startDate = startDate;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }
}
