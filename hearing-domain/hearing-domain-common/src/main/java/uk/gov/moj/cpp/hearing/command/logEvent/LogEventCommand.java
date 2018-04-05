package uk.gov.moj.cpp.hearing.command.logEvent;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.UUID;

public class LogEventCommand {

    private UUID hearingEventId;
    private UUID hearingId;
    private UUID hearingEventDefinitionId;
    private String recordedLabel;
    private ZonedDateTime eventTime;
    private ZonedDateTime lastModifiedTime;
    private Boolean alterable;

    @JsonCreator
    public LogEventCommand(@JsonProperty("hearingEventId") UUID hearingEventId,
                           @JsonProperty("hearingId") UUID hearingId,
                           @JsonProperty("hearingEventDefinitionId") UUID hearingEventDefinitionId,
                           @JsonProperty("recordedLabel") String recordedLabel,
                           @JsonProperty("eventTime") ZonedDateTime eventTime,
                           @JsonProperty("lastModifiedTime") ZonedDateTime lastModifiedTime,
                           @JsonProperty("alterable") Boolean alterable) {

        this.hearingEventId = hearingEventId;
        this.hearingId = hearingId;
        this.hearingEventDefinitionId = hearingEventDefinitionId;
        this.recordedLabel = recordedLabel;
        this.eventTime = eventTime;
        this.lastModifiedTime = lastModifiedTime;
        this.alterable = alterable;
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getHearingEventDefinitionId() {
        return hearingEventDefinitionId;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getAlterable() {
        return alterable;
    }


    public static class Builder {
        private UUID hearingEventId;
        private UUID hearingId;
        private UUID hearingEventDefinitionId;
        private String recordedLabel;
        private ZonedDateTime eventTime;
        private ZonedDateTime lastModifiedTime;
        private Boolean alterable;

        public Builder withHearingEventId(UUID hearingEventId) {
            this.hearingEventId = hearingEventId;
            return this;
        }

        public Builder withHearingId(UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withHearingEventDefinitionId(UUID hearingEventDefinitionId) {
            this.hearingEventDefinitionId = hearingEventDefinitionId;
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

        public Builder withAlterable(Boolean alterable) {
            this.alterable = alterable;
            return this;
        }

        public LogEventCommand build() {
            return new LogEventCommand(hearingEventId, hearingId, hearingEventDefinitionId, recordedLabel, eventTime, lastModifiedTime, alterable);
        }
    }

    public static Builder builder(){
        return new Builder();
    }
}