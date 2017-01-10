package uk.gov.moj.cpp.hearing.domain.event;

import java.time.ZonedDateTime;
import java.util.UUID;
import uk.gov.justice.domain.annotation.Event;

@Event("hearing.hearing-event-corrected")
public class HearingEventCorrected {

    private final UUID hearingId;
    private final UUID hearingEventId;
    private final ZonedDateTime timestamp;

    public HearingEventCorrected(UUID hearingId, UUID hearingEventId, ZonedDateTime timestamp) {
        this.hearingId = hearingId;
        this.hearingEventId = hearingEventId;
        this.timestamp = timestamp;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }
}
