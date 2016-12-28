package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Event("hearing.hearing-event-logged")
public class HearingEventLogged {

    private final UUID id;
    private final UUID hearingId;
    private final String recordedLabel;
    private final ZonedDateTime timestamp;

    public HearingEventLogged(final UUID id, final UUID hearingId, final String recordedLabel,
                              final ZonedDateTime timestamp) {

        this.id = id;
        this.hearingId = hearingId;
        this.recordedLabel = recordedLabel;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
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
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getHearingId(), that.getHearingId()) &&
                Objects.equals(getRecordedLabel(), that.getRecordedLabel()) &&
                Objects.equals(getTimestamp(), that.getTimestamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getHearingId(), getRecordedLabel(), getTimestamp());
    }

    @Override
    public String toString() {
        return "HearingEventLogged{" +
                "id=" + id +
                ", hearingId=" + hearingId +
                ", recordedLabel='" + recordedLabel + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
