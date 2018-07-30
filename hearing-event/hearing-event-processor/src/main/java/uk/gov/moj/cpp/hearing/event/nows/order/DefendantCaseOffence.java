package uk.gov.moj.cpp.hearing.event.nows.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DefendantCaseOffence implements Serializable {

    private final static long serialVersionUID = 2458215902136858208L;
    private String wording;
    private String startDate;
    private String convictionDate;
    private List<OrderResult> results = new ArrayList<OrderResult>();

    public static Builder builder() {
        return new Builder();
    }

    public String getWording() {
        return wording;
    }

    public void setWording(String wording) {
        this.wording = wording;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getConvictionDate() {
        return convictionDate;
    }

    public void setConvictionDate(String convictionDate) {
        this.convictionDate = convictionDate;
    }

    public List<OrderResult> getResults() {
        return results;
    }

    public void setResults(List<OrderResult> results) {
        this.results = results;
    }

    public static final class Builder {
        private String wording;
        private String startDate;
        private String convictionDate;
        private List<OrderResult> results = new ArrayList<OrderResult>();

        private Builder() {
        }


        public Builder withWording(String wording) {
            this.wording = wording;
            return this;
        }

        public Builder withStartDate(String startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder withConvictionDate(String convictionDate) {
            this.convictionDate = convictionDate;
            return this;
        }

        public Builder withResults(List<OrderResult> results) {
            this.results = results;
            return this;
        }

        public DefendantCaseOffence build() {
            DefendantCaseOffence defendantCaseOffence = new DefendantCaseOffence();
            defendantCaseOffence.setWording(wording);
            defendantCaseOffence.setStartDate(startDate);
            defendantCaseOffence.setConvictionDate(convictionDate);
            defendantCaseOffence.setResults(results);
            return defendantCaseOffence;
        }
    }
}
