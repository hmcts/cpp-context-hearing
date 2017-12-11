package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.adjourn-date-updated")
public class HearingAdjournDateUpdated {

    private UUID hearingId;
    private LocalDate startDate;

    public HearingAdjournDateUpdated(final UUID hearingId, final LocalDate startDate) {
        this.hearingId = hearingId;
        this.startDate = startDate;
    }

    public HearingAdjournDateUpdated() {
        // default constructor for Jackson serialisation
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }
}
