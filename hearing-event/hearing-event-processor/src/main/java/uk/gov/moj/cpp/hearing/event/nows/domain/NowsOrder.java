package uk.gov.moj.cpp.hearing.event.nows.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NowsOrder implements Serializable {

    private final static long serialVersionUID = -781257956072320445L;
    private String orderName;
    private NowsOrderCourtCentre courtCentre;
    private String courtClerkName;
    private NowsOrderDefendant defendant;
    private List<String> caseUrns = new ArrayList<String>();
    private List<NowsOrderCase> cases = new ArrayList<NowsOrderCase>();
    private List<NowsOrderResult> defendantResults = new ArrayList<NowsOrderResult>();
    private String staticText;
    private String staticTextWelsh;

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public NowsOrderCourtCentre getCourtCentre() {
        return courtCentre;
    }

    public void setCourtCentre(NowsOrderCourtCentre courtCentre) {
        this.courtCentre = courtCentre;
    }

    public String getCourtClerkName() {
        return courtClerkName;
    }

    public void setCourtClerkName(String courtClerkName) {
        this.courtClerkName = courtClerkName;
    }

    public NowsOrderDefendant getDefendant() {
        return defendant;
    }

    public void setDefendant(NowsOrderDefendant defendant) {
        this.defendant = defendant;
    }

    public List<String> getCaseUrns() {
        return caseUrns;
    }

    public void setCaseUrns(List<String> caseUrns) {
        this.caseUrns = caseUrns;
    }

    public List<NowsOrderCase> getCases() {
        return cases;
    }

    public void setCases(List<NowsOrderCase> cases) {
        this.cases = cases;
    }

    public List<NowsOrderResult> getDefendantResults() {
        return defendantResults;
    }

    public void setDefendantResults(List<NowsOrderResult> defendantResults) {
        this.defendantResults = defendantResults;
    }

    public String getStaticText() {
        return staticText;
    }

    public void setStaticText(String staticText) {
        this.staticText = staticText;
    }

    public String getStaticTextWelsh() {
        return staticTextWelsh;
    }

    public void setStaticTextWelsh(String staticTextWelsh) {
        this.staticTextWelsh = staticTextWelsh;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String orderName;
        private NowsOrderCourtCentre courtCentre;
        private String courtClerkName;
        private NowsOrderDefendant defendant;
        private List<String> caseUrns = new ArrayList<String>();
        private List<NowsOrderCase> cases = new ArrayList<NowsOrderCase>();
        private List<NowsOrderResult> defendantResults = new ArrayList<NowsOrderResult>();
        private String staticText;
        private String staticTextWelsh;

        private Builder() {
        }

        public Builder withOrderName(String orderName) {
            this.orderName = orderName;
            return this;
        }

        public Builder withCourtCentre(NowsOrderCourtCentre courtCentre) {
            this.courtCentre = courtCentre;
            return this;
        }

        public Builder withCourtClerkName(String courtClerkName) {
            this.courtClerkName = courtClerkName;
            return this;
        }

        public Builder withDefendant(NowsOrderDefendant defendant) {
            this.defendant = defendant;
            return this;
        }

        public Builder withCaseUrns(List<String> caseUrns) {
            this.caseUrns = caseUrns;
            return this;
        }

        public Builder withCases(List<NowsOrderCase> cases) {
            this.cases = cases;
            return this;
        }

        public Builder withDefendantResults(List<NowsOrderResult> defendantResults) {
            this.defendantResults = defendantResults;
            return this;
        }

        public Builder withStaticText(String staticText) {
            this.staticText = staticText;
            return this;
        }

        public Builder withStaticTextWelsh(String staticTextWelsh) {
            this.staticTextWelsh = staticTextWelsh;
            return this;
        }

        public NowsOrder build() {
            NowsOrder nowsOrder = new NowsOrder();
            nowsOrder.setOrderName(orderName);
            nowsOrder.setCourtCentre(courtCentre);
            nowsOrder.setCourtClerkName(courtClerkName);
            nowsOrder.setDefendant(defendant);
            nowsOrder.setCaseUrns(caseUrns);
            nowsOrder.setCases(cases);
            nowsOrder.setDefendantResults(defendantResults);
            nowsOrder.setStaticText(staticText);
            nowsOrder.setStaticTextWelsh(staticTextWelsh);
            return nowsOrder;
        }
    }
}
