package uk.gov.moj.cpp.hearing.event.nows.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class OrderCase implements Serializable {
    private final static long serialVersionUID = 8018789124132562641L;

    private String urn;

    private List<OrderResult> defendantCaseResults = new ArrayList<OrderResult>();
    private List<DefendantCaseOffence> defendantCaseOffences = new ArrayList<DefendantCaseOffence>();
    private List<OrderResult> caseResults = new ArrayList<OrderResult>();

    public List<OrderResult> getDefendantCaseResults() {
        return defendantCaseResults;
    }

    public void setDefendantCaseResults(List<OrderResult> defendantCaseResults) {
        this.defendantCaseResults = defendantCaseResults;
    }

    public List<DefendantCaseOffence> getDefendantCaseOffences() {
        return defendantCaseOffences;
    }

    public void setDefendantCaseOffences(List<DefendantCaseOffence> defendantCaseOffences) {
        this.defendantCaseOffences = defendantCaseOffences;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public List<OrderResult> getCaseResults() {
        return caseResults;
    }

    public void setCaseResults(List<OrderResult> caseResults) {
        this.caseResults = caseResults;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String urn;
        private List<OrderResult> defendantCaseResults = new ArrayList<OrderResult>();
        private List<DefendantCaseOffence> defendantCaseOffences = new ArrayList<DefendantCaseOffence>();
        private List<OrderResult> caseResults = new ArrayList<OrderResult>();

        private Builder() {
        }

        public Builder withUrn(String urn) {
            this.urn = urn;
            return this;
        }

        public Builder withDefendantCaseResults(List<OrderResult> defendantCaseResults) {
            this.defendantCaseResults = defendantCaseResults;
            return this;
        }

        public Builder withDefendantCaseOffences(List<DefendantCaseOffence> defendantCaseOffences) {
            this.defendantCaseOffences = defendantCaseOffences;
            return this;
        }

        public Builder withCaseResults(List<OrderResult> caseResults) {
            this.caseResults = caseResults;
            return this;
        }

        public OrderCase build() {
            OrderCase orderCase = new OrderCase();
            orderCase.setUrn(urn);
            orderCase.setDefendantCaseResults(defendantCaseResults);
            orderCase.setDefendantCaseOffences(defendantCaseOffences);
            orderCase.setCaseResults(caseResults);
            return orderCase;
        }
    }
}
