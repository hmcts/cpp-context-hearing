package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public class Case implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID caseId;
    private final String urn;

    @JsonCreator
    public Case(@JsonProperty("caseId") UUID caseId, @JsonProperty("urn") String urn) {
        this.caseId = caseId;
        this.urn = urn;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getUrn() {
        return urn;
    }

    public static class Builder {

        private UUID caseId;
        private String urn;

        private Builder() {

        }

        public UUID getCaseId() {
            return caseId;
        }

        public String getUrn() {
            return urn;
        }

        public Builder withCaseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withUrn(String urn) {
            this.urn = urn;
            return this;
        }

        public Case build() {
            return new Case(caseId, urn);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(Case legalCase) {
        return builder()
                .withCaseId(legalCase.getCaseId())
                .withUrn(legalCase.getUrn());
    }
}
