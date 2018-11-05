package uk.gov.moj.cpp.hearing.event.message.eventlog;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.ZonedDateTime;
import java.util.UUID;

public class HearingEvent {
    private UUID hearingEventId;
    private UUID lastHearingEventId;
    private String recordedLabel;
    private ZonedDateTime eventTime;
    private ZonedDateTime lastModifiedTime;

    public HearingEvent(UUID hearingEventId, UUID lastHearingEventId, String recordedLabel, ZonedDateTime eventTime, ZonedDateTime lastModifiedTime) {
        this.hearingEventId = hearingEventId;
        this.lastHearingEventId = lastHearingEventId;
        this.recordedLabel = recordedLabel;
        this.eventTime = eventTime;
        this.lastModifiedTime = lastModifiedTime;
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public UUID getLastHearingEventId() {
        return lastHearingEventId;
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

    public static class Builder {
        private UUID hearingEventId;
        private UUID lastHearingEventId;
        private String recordedLabel;
        private ZonedDateTime eventTime;
        private ZonedDateTime lastModifiedTime;

        public Builder withHearingEventId(UUID hearingEventId) {
            this.hearingEventId = hearingEventId;
            return this;
        }

        public Builder withLastHearingEventId(UUID lastHearingEventId) {
            this.lastHearingEventId = lastHearingEventId;
            return this;
        }

        public Builder withRecordedLabel(String recordedLabel) {
            this.recordedLabel = recordedLabel;
            return this;
        }

        public Builder withEventTime(ZonedDateTime eventTime) {
            this.eventTime = eventTime;
            return this;
        }

        public Builder withLastModifiedTime(ZonedDateTime lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
            return this;
        }

        public HearingEvent build() {
            return new HearingEvent(this.hearingEventId, this.lastHearingEventId, this.recordedLabel, this.eventTime, this.lastModifiedTime);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}