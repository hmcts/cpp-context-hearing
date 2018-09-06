package uk.gov.moj.cpp.hearing.eventlog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Case {

    private String caseUrn;

    @JsonCreator
    public Case(@JsonProperty("caseUrn") final String caseUrn) {
        this.caseUrn = caseUrn;
    }

    public String getCaseUrn() {
        return caseUrn;
    }

    public static class Builder {
        private String caseUrn;

        public Builder withCaseUrn(String caseUrn) {
            this.caseUrn = caseUrn;
            return this;
        }

        public Case build() {
            return new Case(this.caseUrn);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
