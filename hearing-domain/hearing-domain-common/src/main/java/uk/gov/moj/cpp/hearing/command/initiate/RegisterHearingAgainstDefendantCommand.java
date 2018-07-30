package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public class RegisterHearingAgainstDefendantCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID defendantId;

    private final UUID hearingId;

    @JsonCreator
    public RegisterHearingAgainstDefendantCommand(@JsonProperty("defendantId") UUID defendantId, @JsonProperty("hearingId") UUID hearingId) {
        this.defendantId = defendantId;
        this.hearingId = hearingId;
    }

    public static Builder builder() {
        return new RegisterHearingAgainstDefendantCommand.Builder();
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public static class Builder {

        private UUID defendantId;

        private UUID hearingId;

        private Builder() {
        }

        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public RegisterHearingAgainstDefendantCommand build() {
            return new RegisterHearingAgainstDefendantCommand(defendantId, hearingId);
        }
    }
}
