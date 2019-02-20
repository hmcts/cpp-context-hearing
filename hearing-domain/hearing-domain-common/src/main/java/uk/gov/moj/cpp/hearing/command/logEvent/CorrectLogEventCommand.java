package uk.gov.moj.cpp.hearing.command.logEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CorrectLogEventCommand {

    private final UUID hearingEventId;
    private final UUID latestHearingEventId;
    private final UUID hearingId;
    private final UUID hearingEventDefinitionId;
    private final String recordedLabel;
    private final ZonedDateTime eventTime;
    private final ZonedDateTime lastModifiedTime;
    private final boolean alterable;
    private final UUID defenceCounselId;

    @JsonCreator
    public CorrectLogEventCommand(@JsonProperty("hearingEventId") final UUID hearingEventId,
                                  @JsonProperty("latestHearingEventId") final UUID latestHearingEventId,
                                  @JsonProperty("hearingId") final UUID hearingId,
                                  @JsonProperty("hearingEventDefinitionId") final UUID hearingEventDefinitionId,
                                  @JsonProperty("recordedLabel") final String recordedLabel,
                                  @JsonProperty("eventTime") final ZonedDateTime eventTime,
                                  @JsonProperty("lastModifiedTime") final ZonedDateTime lastModifiedTime,
                                  @JsonProperty("alterable") final boolean alterable,
                                  @JsonProperty("defenceCounselId") final UUID defenceCounselId) {
        this.hearingEventId = hearingEventId;
        this.latestHearingEventId = latestHearingEventId;
        this.hearingId = hearingId;
        this.hearingEventDefinitionId = hearingEventDefinitionId;
        this.recordedLabel = recordedLabel;
        this.eventTime = eventTime;
        this.lastModifiedTime = lastModifiedTime;
        this.alterable = alterable;
        this.defenceCounselId = defenceCounselId;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public UUID getHearingEventId() {
        return hearingEventId;
    }

    public UUID getLatestHearingEventId() {
        return latestHearingEventId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
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

    public UUID getDefenceCounselId() {
        return defenceCounselId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public boolean getAlterable() {
        return alterable;
    }

    public static class Builder {

        private UUID hearingEventId;
        private UUID latestHearingEventId;
        private UUID hearingId;
        private UUID hearingEventDefinitionId;
        private String recordedLabel;
        private ZonedDateTime eventTime;
        private ZonedDateTime lastModifiedTime;
        private boolean alterable;
        private UUID defenceCounselId;

        public Builder withHearingEventId(final UUID hearingEventId) {
            this.hearingEventId = hearingEventId;
            return this;
        }

        public Builder withLastestHearingEventId(final UUID latestHearingEventId) {
            this.latestHearingEventId = latestHearingEventId;
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

        public Builder withEventTime(final ZonedDateTime eventTime) {
            this.eventTime = eventTime;
            return this;
        }

        public Builder withLastModifiedTime(final ZonedDateTime lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
            return this;
        }

        public Builder withAlterable(final boolean alterable) {
            this.alterable = alterable;
            return this;
        }

        public Builder withDefenceCounselId(final UUID counselId) {
            this.defenceCounselId = counselId;
            return this;
        }

        public CorrectLogEventCommand build() {
            return new CorrectLogEventCommand(this.hearingEventId, this.latestHearingEventId, this.hearingId,
                    this.hearingEventDefinitionId, this.recordedLabel, this.eventTime, this.lastModifiedTime,
                    this.alterable, this.defenceCounselId);
        }
    }
}
