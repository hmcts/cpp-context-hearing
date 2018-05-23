package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public class RegisterCaseWithHearingCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID caseId;

    private final UUID hearingId;

    @JsonCreator
    private RegisterCaseWithHearingCommand(@JsonProperty("caseId") UUID caseId, @JsonProperty("hearingId") UUID hearingId) {
        this.caseId = caseId;
        this.hearingId = hearingId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID caseId;

        private UUID hearingId;

        private Builder() {
        }

        public Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public RegisterCaseWithHearingCommand build() {
            return new RegisterCaseWithHearingCommand(caseId, hearingId);
        }
    }
}
