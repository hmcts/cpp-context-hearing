package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.ZonedDateTime;
import java.util.UUID;

@Event("hearing.started")
public class HearingStarted {

    private final UUID hearingId;

    private final ZonedDateTime startTime;

    public HearingStarted(final UUID hearingId, final ZonedDateTime startTime) {
        this.hearingId = hearingId;
        this.startTime = startTime;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }
}
