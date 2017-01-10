package uk.gov.moj.cpp.hearing.domain.aggregate;

import java.time.ZonedDateTime;
import java.util.UUID;

public class HearingEvent {

    private UUID hearingEventId;
    private ZonedDateTime timestamp;

    public HearingEvent(UUID hearingEventId, ZonedDateTime timestamp) {
        this.hearingEventId = hearingEventId;
        this.timestamp = timestamp;
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
