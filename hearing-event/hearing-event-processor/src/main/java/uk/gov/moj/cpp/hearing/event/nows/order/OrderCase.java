package uk.gov.moj.cpp.hearing.event.nows.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class OrderCase implements Serializable {
    private final static long serialVersionUID = 8018789124132562641L;

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
        private List<OrderResult> defendantCaseResults = new ArrayList<OrderResult>();
        private List<DefendantCaseOffence> defendantCaseOffences = new ArrayList<DefendantCaseOffence>();
        private List<OrderResult> caseResults = new ArrayList<OrderResult>();

        private Builder() {
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
            OrderCase aCase = new OrderCase();
            aCase.setDefendantCaseResults(defendantCaseResults);
            aCase.setDefendantCaseOffences(defendantCaseOffences);
            aCase.setCaseResults(caseResults);
            return aCase;
        }
    }
}
