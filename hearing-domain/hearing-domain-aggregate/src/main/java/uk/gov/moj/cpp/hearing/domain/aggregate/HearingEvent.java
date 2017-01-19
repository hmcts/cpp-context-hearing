package uk.gov.moj.cpp.hearing.domain.aggregate;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class HearingEvent {

    private final UUID hearingEventId;
    private final ZonedDateTime timestamp;

    public HearingEvent(final UUID hearingEventId, final ZonedDateTime timestamp) {
        this.hearingEventId = hearingEventId;
        this.timestamp = timestamp;
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HearingEvent that = (HearingEvent) o;
        return Objects.equals(getHearingEventId(), that.getHearingEventId()) &&
                Objects.equals(getTimestamp(), that.getTimestamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHearingEventId(), getTimestamp());
    }
}
