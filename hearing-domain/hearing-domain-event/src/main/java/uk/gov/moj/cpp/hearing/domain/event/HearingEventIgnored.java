package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Event("hearing.hearing-event-ignored")
public class HearingEventIgnored {

    private final UUID hearingEventId;
    private final UUID hearingId;
    private final String recordedLabel;
    private final ZonedDateTime timestamp;
    private final String reason;

    public HearingEventIgnored(final UUID hearingEventId, final UUID hearingId, final String recordedLabel,
                               final ZonedDateTime timestamp, final String reason) {
        this.hearingEventId = hearingEventId;
        this.hearingId = hearingId;
        this.recordedLabel = recordedLabel;
        this.timestamp = timestamp;
        this.reason = reason;
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public String getRecordedLabel() {
        return recordedLabel;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HearingEventIgnored that = (HearingEventIgnored) o;
        return Objects.equals(getHearingEventId(), that.getHearingEventId()) &&
                Objects.equals(getHearingId(), that.getHearingId()) &&
                Objects.equals(getRecordedLabel(), that.getRecordedLabel()) &&
                Objects.equals(getTimestamp(), that.getTimestamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHearingEventId(), getHearingId(), getRecordedLabel(), getTimestamp());
    }

    @Override
    public String toString() {
        return "HearingEventIgnored{" +
                "hearingEventId=" + hearingEventId +
                ", hearingId=" + hearingId +
                ", recordedLabel='" + recordedLabel + '\'' +
                ", timestamp=" + timestamp +
                ", reason='" + reason + '\'' +
                '}';
    }
}
