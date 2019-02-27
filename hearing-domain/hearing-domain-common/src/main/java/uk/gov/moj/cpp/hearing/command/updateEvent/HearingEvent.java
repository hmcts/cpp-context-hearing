package uk.gov.moj.cpp.hearing.command.updateEvent;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HearingEvent implements Serializable {

    private final UUID hearingEventId;
    private final String recordedLabel;

    @JsonCreator
    public HearingEvent(@JsonProperty("hearingEventId") final UUID hearingEventId,
                        @JsonProperty("recordedLabel") final String recordedLabel) {
        this.hearingEventId = hearingEventId;
        this.recordedLabel = recordedLabel;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    public String getRecordedLabel() {
        return recordedLabel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.hearingEventId, this.recordedLabel);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HearingEvent other = (HearingEvent) obj;
        return Objects.equals(this.hearingEventId, other.hearingEventId) && Objects.equals(this.recordedLabel, other.recordedLabel);
    }

    @Override
    public String toString() {
        return "HearingEvent [hearingEventId=" + hearingEventId + ", recordedLabel=" + recordedLabel + "]";
    }

    public static final class Builder {

        private UUID hearingEventId;
        private String recordedLabel;

        public Builder withHearingEventId(final UUID hearingEventId) {
            this.hearingEventId = hearingEventId;
            return this;
        }

        public Builder withRecordedLabel(final String recordedLabel) {
            this.recordedLabel = recordedLabel;
            return this;
        }

        public HearingEvent build() {
            return new HearingEvent(hearingEventId, recordedLabel);
        }
    }
}