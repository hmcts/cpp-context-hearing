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

    @Column(name = "deleted")
    private boolean deleted;

    public HearingEvent() {
        // for JPA
    }

    public HearingEvent(final UUID id, final UUID hearingId, final String recordedLabel,
                        final ZonedDateTime timestamp) {
        this.id = id;
        this.hearingId = hearingId;
        this.recordedLabel = recordedLabel;
        this.timestamp = timestamp;
        this.deleted = false;
    }

    private HearingEvent(final UUID id, final UUID hearingId, final String recordedLabel,
                        final ZonedDateTime timestamp, final boolean deleted) {
        this.id = id;
        this.hearingId = hearingId;
        this.recordedLabel = recordedLabel;
        this.timestamp = timestamp;
        this.deleted = deleted;
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

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HearingEvent that = (HearingEvent) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "HearingEvent{" +
                "id=" + id +
                ", hearingId=" + hearingId +
                ", recordedLabel='" + recordedLabel + '\'' +
                ", timestamp=" + timestamp +
                ", deleted=" + deleted +
                '}';
    }

    public Builder builder() {
        return new Builder(getId(), getHearingId(), getRecordedLabel(), getTimestamp(), isDeleted());
    }

    public class Builder {
        private UUID id;
        private UUID hearingId;
        private String recordedLabel;
        private ZonedDateTime timestamp;
        private boolean deleted;

        public Builder(final UUID id, final UUID hearingId, final String recordedLabel,
                       final ZonedDateTime timestamp, final boolean deleted) {
            this.id = id;
            this.hearingId = hearingId;
            this.recordedLabel = recordedLabel;
            this.timestamp = timestamp;
            this.deleted = deleted;
        }

        public Builder withId(final UUID hearingEventId) {
            this.id = hearingEventId;
            return this;
        }

        public Builder withTimestamp(final ZonedDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder delete() {
            this.deleted = true;
            return this;
        }

        public HearingEvent build() {
            return new HearingEvent(this.id, this.hearingId, this.recordedLabel, this.timestamp, this.deleted);
        }
    }

}
