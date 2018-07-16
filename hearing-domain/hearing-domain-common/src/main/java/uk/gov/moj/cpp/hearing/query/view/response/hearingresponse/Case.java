package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@SuppressWarnings({"squid:UnusedPrivateMethod"})
public class Case {

    private final String caseId;
    private final String caseUrn;
    private final List<Defendant> defendants;
    private final List<Witness> witnesses;

    public Case() {
        this.caseId = null;
        this.caseUrn = null;
        this.defendants = null;
        this.witnesses = null;
    }


    private Case(@JsonProperty("caseId") final String caseId,
                 @JsonProperty("caseUrn") final String caseUrn,
                 @JsonProperty("defendants") final List<Defendant> defendants,
                 @JsonProperty("witnesses") final List<Witness> witnesses) {
        this.caseId = caseId;
        this.caseUrn = caseUrn;
        this.defendants = defendants;
        this.witnesses = witnesses;
    }

    private Case(final Builder builder) {
        this.caseId = builder.caseId;
        this.caseUrn = builder.caseUrn;
        this.defendants = builder.defendants;
        this.witnesses = builder.witnesses;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCaseId() {
        return caseId;
    }

    public String getCaseUrn() {
        return caseUrn;
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public List<Witness> getWitnesses() {
        return witnesses;
    }

    public static final class Builder {

        private String caseId;
        private String caseUrn;
        private List<Defendant> defendants;
        private List<Witness> witnesses;

        public Builder withCaseId(final String caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withCaseUrn(final String caseUrn) {
            this.caseUrn = caseUrn;
            return this;
        }

        public Builder withDefendants(final List<Defendant> defendants) {
            this.defendants = defendants;
            return this;
        }

        public Builder withWitnesses(List<Witness> witnesses) {
            this.witnesses = witnesses;
            return this;
        }

        public Case build() {
            return new Case(this);
        }
    }
}