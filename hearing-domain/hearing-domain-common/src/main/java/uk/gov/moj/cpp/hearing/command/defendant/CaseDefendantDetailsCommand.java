package uk.gov.moj.cpp.hearing.command.defendant;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

import static java.util.Optional.ofNullable;

public class CaseDefendantDetailsCommand {

    private final UUID caseId;

    private final Defendant defendant;

    private CaseDefendantDetailsCommand(
            @JsonProperty("caseId") UUID caseId,
            @JsonProperty("defendant") final Defendant defendant) {
        this.caseId = caseId;
        this.defendant = defendant;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getCaseId() {
        return caseId;
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public static class Builder {

        private UUID caseId;

        private Defendant.Builder defendent;

        private Builder() {

        }

        public Builder withCaseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withDefendant(Defendant.Builder defendent) {
            this.defendent = defendent;
            return this;
        }

        public CaseDefendantDetailsCommand build() {
            return new CaseDefendantDetailsCommand(caseId, ofNullable(defendent).map(Defendant.Builder::build).orElse(null));
        }
    }
}
