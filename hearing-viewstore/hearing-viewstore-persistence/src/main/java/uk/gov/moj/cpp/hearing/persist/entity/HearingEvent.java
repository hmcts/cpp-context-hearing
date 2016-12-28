package uk.gov.moj.cpp.hearing.persist.entity;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "hearing_event")
public class HearingEvent {

    @Id
    private UUID id;

    @Column(name = "hearing_id")
    private UUID hearingId;

    @Column(name = "recorded_label")
    private String recordedLabel;

    @Column(name = "timestamp")
    private ZonedDateTime timestamp;

    public HearingEvent() {
        // for JPA
    }

    public HearingEvent(final UUID id, final UUID hearingId, final String recordedLabel,
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
        HearingEvent that = (HearingEvent) o;
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
        return "HearingEvent{" +
                "id=" + id +
                ", hearingId=" + hearingId +
                ", recordedLabel='" + recordedLabel + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
