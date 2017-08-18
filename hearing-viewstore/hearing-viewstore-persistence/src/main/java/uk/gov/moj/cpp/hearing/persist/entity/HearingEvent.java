package uk.gov.moj.cpp.hearing.persist.entity;

import java.time.ZonedDateTime;
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

    @Column(name = "hearing_event_definition_id")
    private UUID hearingEventDefinitionId;

    @Column(name = "hearing_id")
    private UUID hearingId;

    @Column(name = "recorded_label")
    private String recordedLabel;

    @Column(name = "event_time")
    private ZonedDateTime eventTime;

    @Column(name = "last_modified_time")
    private ZonedDateTime lastModifiedTime;

    @Column(name = "alterable")
    private boolean alterable;

    @Column(name = "deleted")
    private boolean deleted;

    public HearingEvent() {
        // for JPA
    }

    public HearingEvent(final UUID id, final UUID hearingEventDefinitionId, final UUID hearingId, final String recordedLabel,
                        final ZonedDateTime eventTime, final ZonedDateTime lastModifiedTime,
                        final boolean alterable) {
        this.id = id;
        this.hearingEventDefinitionId = hearingEventDefinitionId;
        this.hearingId = hearingId;
        this.recordedLabel = recordedLabel;
        this.eventTime = eventTime;
        this.lastModifiedTime = lastModifiedTime;
        this.alterable = alterable;
        this.deleted = false;
    }

    private HearingEvent(final UUID id, final UUID hearingEventDefinitionId, final UUID hearingId, final String recordedLabel,
                         final ZonedDateTime eventTime, final ZonedDateTime lastModifiedTime,
                         final boolean alterable, final boolean deleted) {
        this.id = id;
        this.hearingEventDefinitionId = hearingEventDefinitionId;
        this.hearingId = hearingId;
        this.recordedLabel = recordedLabel;
        this.eventTime = eventTime;
        this.lastModifiedTime = lastModifiedTime;
        this.alterable = alterable;
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

    public ZonedDateTime getEventTime() {
        return eventTime;
    }

    public ZonedDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public boolean isAlterable() {
        return alterable;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Builder builder() {
        return new Builder(getId(), getHearingEventDefinitionId(), getHearingId(), getRecordedLabel(), getEventTime(), getLastModifiedTime(), isAlterable(), isDeleted());
    }

    public UUID getHearingEventDefinitionId() {
        return hearingEventDefinitionId;
    }

    public class Builder {
        private UUID id;
        private UUID hearingId;
        private UUID hearingEventDefinitionId;
        private String recordedLabel;
        private ZonedDateTime eventTime;
        private ZonedDateTime lastModifiedTime;
        private boolean alterable;
        private boolean deleted;

        Builder(final UUID id, final UUID hearingEventDefinitionId, final UUID hearingId, final String recordedLabel,
                final ZonedDateTime eventTime, final ZonedDateTime lastModifiedTime,
                final boolean alterable, final boolean deleted) {
            this.id = id;
            this.hearingEventDefinitionId = hearingEventDefinitionId;
            this.hearingId = hearingId;
            this.recordedLabel = recordedLabel;
            this.eventTime = eventTime;
            this.lastModifiedTime = lastModifiedTime;
            this.alterable = alterable;
            this.deleted = deleted;
        }

        public Builder withId(final UUID hearingEventId) {
            this.id = hearingEventId;
            return this;
        }

        public Builder withEventTime(final ZonedDateTime eventTime) {
            this.eventTime = eventTime;
            return this;
        }

        public Builder withLastModifiedTime(final ZonedDateTime lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
            return this;
        }

        public Builder delete() {
            this.deleted = true;
            return this;
        }

        public HearingEvent build() {
            return new HearingEvent(this.id, this.hearingEventDefinitionId, this.hearingId, this.recordedLabel, this.eventTime, this.lastModifiedTime, this.alterable, this.deleted);
        }
    }

}
