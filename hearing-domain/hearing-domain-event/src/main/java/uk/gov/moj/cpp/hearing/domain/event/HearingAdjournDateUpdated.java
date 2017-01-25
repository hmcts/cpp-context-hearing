package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Event("hearing.adjourn-date-updated")
public class HearingAdjournDateUpdated {

    private UUID hearingId;

    private LocalDate startDate;

    public HearingAdjournDateUpdated(UUID hearingId, LocalDate startDate) {
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
