package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.ZonedDateTime;
import java.util.UUID;

@Event("hearing.ended")
public class HearingEnded {

    private UUID hearingId;

    private ZonedDateTime endTime;

    public HearingEnded(UUID hearingId, ZonedDateTime endTime) {
        this.hearingId = hearingId;
        this.endTime = endTime;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }
}
