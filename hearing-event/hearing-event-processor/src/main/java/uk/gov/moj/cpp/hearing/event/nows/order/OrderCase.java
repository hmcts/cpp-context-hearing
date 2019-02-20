package uk.gov.moj.cpp.hearing.event.nows.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class OrderCase implements Serializable {

    private final static long serialVersionUID = 2L;

    private String urn;

    private List<OrderResult> defendantCaseResults = new ArrayList<OrderResult>();

    private List<DefendantCaseOffence> defendantCaseOffences = new ArrayList<DefendantCaseOffence>();

    private List<OrderResult> caseResults = new ArrayList<OrderResult>();

    public static OrderCase orderCase() {
        return new OrderCase();
    }

    public List<OrderResult> getDefendantCaseResults() {
        return defendantCaseResults;
    }

    public OrderCase setDefendantCaseResults(List<OrderResult> defendantCaseResults) {
        this.defendantCaseResults = defendantCaseResults;
        return this;
    }

    public List<DefendantCaseOffence> getDefendantCaseOffences() {
        return defendantCaseOffences;
    }

    public OrderCase setDefendantCaseOffences(List<DefendantCaseOffence> defendantCaseOffences) {
        this.defendantCaseOffences = defendantCaseOffences;
        return this;
    }

    public String getUrn() {
        return urn;
    }

    public OrderCase setUrn(String urn) {
        this.urn = urn;
        return this;
    }

    public List<OrderResult> getCaseResults() {
        return caseResults;
    }

    public OrderCase setCaseResults(List<OrderResult> caseResults) {
        this.caseResults = caseResults;
        return this;
    }
}
