package uk.gov.moj.cpp.hearing.command.defendant;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

import static java.util.Optional.ofNullable;

public class CaseDefendantDetailsCommand {

    private final UUID caseId;

    private final String caseUrn;

    private final Defendant defendant;

    public CaseDefendantDetailsCommand(
            @JsonProperty("caseId") UUID caseId,
            @JsonProperty("caseUrn") String caseUrn,
            @JsonProperty("defendant") final Defendant defendant) {
        this.caseId = caseId;
        this.caseUrn = caseUrn;
        this.defendant = defendant;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getCaseUrn() {
        return caseUrn;
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public static class Builder {

        private UUID caseId;

        private String caseUrn;

        private Defendant.Builder defendent;

        private Builder() {

        }

        public Builder withCaseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withCaseUrn(String caseUrn) {
            this.caseUrn = caseUrn;
            return this;
        }

        public Builder withDefendant(Defendant.Builder defendent) {
            this.defendent = defendent;
            return this;
        }

        public CaseDefendantDetailsCommand build() {
            return new CaseDefendantDetailsCommand(caseId, caseUrn, ofNullable(defendent).map(Defendant.Builder::build).orElse(null));
        }
    }

}
