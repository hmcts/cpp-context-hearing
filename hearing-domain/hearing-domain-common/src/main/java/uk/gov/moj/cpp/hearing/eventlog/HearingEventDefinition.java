package uk.gov.moj.cpp.hearing.eventlog;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HearingEventDefinition {

    private UUID hearingEventDefinitionId;
    private boolean priority;

    @JsonCreator
    public HearingEventDefinition(@JsonProperty("hearingEventDefinitionId") final UUID hearingEventDefinitionId,
                                  @JsonProperty("priority") final boolean priority) {
        this.hearingEventDefinitionId = hearingEventDefinitionId;
        this.priority = priority;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getHearingEventDefinitionId() {
        return hearingEventDefinitionId;
    }

    public boolean isPriority() {
        return priority;
    }

    public static class Builder {
        private UUID hearingEventDefinitionId;
        private boolean priority;

        public Builder withHearingEventDefinitionId(UUID hearingEventDefinitionId) {
            this.hearingEventDefinitionId = hearingEventDefinitionId;
            return this;
        }

        public Builder withPriority(boolean priority) {
            this.priority = priority;
            return this;
        }

        public HearingEventDefinition build() {
            return new HearingEventDefinition(this.hearingEventDefinitionId, this.priority);
        }
    }
}