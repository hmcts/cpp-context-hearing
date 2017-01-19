package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Event("hearing.hearing-event-logged")
public class HearingEventLogged {

    private final UUID hearingEventId;
    private final UUID hearingId;
    private final String recordedLabel;
    private final ZonedDateTime timestamp;

    public HearingEventLogged(final UUID hearingEventId, final UUID hearingId, final String recordedLabel,
                              final ZonedDateTime timestamp) {

        this.hearingEventId = hearingEventId;
        this.hearingId = hearingId;
        this.recordedLabel = recordedLabel;
        this.timestamp = timestamp;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HearingEventLogged that = (HearingEventLogged) o;
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
        return "HearingEventLogged{" +
                "hearingEventId=" + hearingEventId +
                ", hearingId=" + hearingId +
                ", recordedLabel='" + recordedLabel + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
