package uk.gov.moj.cpp.hearing.command.logEvent;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LogEventCommand {

    private final UUID hearingEventId;
    private final UUID hearingId;
    private final UUID hearingEventDefinitionId;
    private final String note;
    private final String recordedLabel;
    private final ZonedDateTime eventTime;
    private final ZonedDateTime lastModifiedTime;
    private final Boolean alterable;
    private final UUID defenceCounselId;
    private final List<UUID> hearingTypeIds;

    @JsonCreator
    public LogEventCommand(@JsonProperty("hearingEventId") final UUID hearingEventId,
                           @JsonProperty("hearingId") final UUID hearingId,
                           @JsonProperty("hearingEventDefinitionId") final UUID hearingEventDefinitionId,
                           @JsonProperty("recordedLabel") final String recordedLabel,
                           @JsonProperty("note") final String note,
                           @JsonProperty("eventTime") final ZonedDateTime eventTime,
                           @JsonProperty("lastModifiedTime") final ZonedDateTime lastModifiedTime,
                           @JsonProperty("alterable") final Boolean alterable,
                           @JsonProperty("defenceCounselId") final UUID defenceCounselId,
                           @JsonProperty("hearingTypeIds") final List<UUID> hearingTypeIds) {
        this.hearingEventId = hearingEventId;
        this.hearingId = hearingId;
        this.hearingEventDefinitionId = hearingEventDefinitionId;
        this.recordedLabel = recordedLabel;
        this.note = note;
        this.eventTime = eventTime;
        this.lastModifiedTime = lastModifiedTime;
        this.alterable = alterable;
        this.defenceCounselId = defenceCounselId;
        this.hearingTypeIds = hearingTypeIds;
    }

    public static Builder builder() {
        return new Builder();
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

    public String getNote() {
        return note;
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

    public UUID getDefenceCounselId() {
        return defenceCounselId;
    }

    public List<UUID> getHearingTypeIds() {
        return hearingTypeIds;
    }

    public static class Builder {
        private UUID hearingEventId;
        private UUID hearingId;
        private UUID hearingEventDefinitionId;
        private String recordedLabel;
        private String note;
        private ZonedDateTime eventTime;
        private ZonedDateTime lastModifiedTime;
        private Boolean alterable;
        private UUID defenceCounselId;
        private List<UUID> hearingTypeIds;

        public Builder withHearingEventId(final UUID hearingEventId) {
            this.hearingEventId = hearingEventId;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withHearingEventDefinitionId(final UUID hearingEventDefinitionId) {
            this.hearingEventDefinitionId = hearingEventDefinitionId;
            return this;
        }

        public Builder withRecordedLabel(final String recordedLabel) {
            this.recordedLabel = recordedLabel;
            return this;
        }

        public Builder withNote(final String note) {
            this.note = note;
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

        public Builder withAlterable(final Boolean alterable) {
            this.alterable = alterable;
            return this;
        }

        public Builder withDefenceCounselId(final UUID defenceCounselId) {
            this.defenceCounselId = defenceCounselId;
            return this;
        }

        public Builder withHearingTypeIds(final List<UUID> hearingTypeIds) {
            this.hearingTypeIds = hearingTypeIds;
            return this;
        }

        public LogEventCommand build() {
            return new LogEventCommand(hearingEventId, hearingId, hearingEventDefinitionId,
                    recordedLabel, note, eventTime, lastModifiedTime, alterable, defenceCounselId, hearingTypeIds);
        }
    }
}