package uk.gov.moj.cpp.hearing.event.message.eventlog;

import java.util.UUID;

public class HearingEventDefinition {

    private UUID hearingEventDefinitionId;
    private boolean priority;

    public HearingEventDefinition(UUID hearingEventDefinitionId, boolean priority) {
        this.hearingEventDefinitionId = hearingEventDefinitionId;
        this.priority = priority;
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

    public static Builder builder() {
        return new Builder();
    }
}