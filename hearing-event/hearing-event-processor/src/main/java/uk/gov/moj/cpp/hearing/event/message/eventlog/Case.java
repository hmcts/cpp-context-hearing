package uk.gov.moj.cpp.hearing.event.message.eventlog;

public class Case {
    private String caseUrn;

    public Case(String caseUrn) {
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
