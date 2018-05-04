package uk.gov.moj.cpp.hearing.event.nows.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class NowsOrderCase implements Serializable {
    private final static long serialVersionUID = 8018789124132562641L;
    private String urn;
    private List<DefendantCaseOffence> defendantCaseOffences = new ArrayList<DefendantCaseOffence>();


    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public List<DefendantCaseOffence> getDefendantCaseOffences() {
        return defendantCaseOffences;
    }

    public void setDefendantCaseOffences(List<DefendantCaseOffence> defendantCaseOffences) {
        this.defendantCaseOffences = defendantCaseOffences;
    }
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String urn;
        private List<DefendantCaseOffence> defendantCaseOffences = new ArrayList<DefendantCaseOffence>();

        private Builder() {
        }

        public Builder withUrn(String urn) {
            this.urn = urn;
            return this;
        }

        public Builder withDefendantCaseOffences(List<DefendantCaseOffence> defendantCaseOffences) {
            this.defendantCaseOffences = defendantCaseOffences;
            return this;
        }

        public NowsOrderCase build() {
            NowsOrderCase nowsOrderCase = new NowsOrderCase();
            nowsOrderCase.setUrn(urn);
            nowsOrderCase.setDefendantCaseOffences(defendantCaseOffences);
            return nowsOrderCase;
        }
    }
}
