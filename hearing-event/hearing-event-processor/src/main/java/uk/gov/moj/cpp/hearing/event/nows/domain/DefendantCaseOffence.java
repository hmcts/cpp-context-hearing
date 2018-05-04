package uk.gov.moj.cpp.hearing.event.nows.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DefendantCaseOffence implements Serializable {

    private final static long serialVersionUID = 2458215902136858208L;
    private String wording;
    private String startDate;
    private String convictionDate;
    private List<NowsOrderResult> results = new ArrayList<NowsOrderResult>();

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

    public List<NowsOrderResult> getResults() {
        return results;
    }

    public void setResults(List<NowsOrderResult> results) {
        this.results = results;
    }

    public static final class Builder {
        private String wording;
        private String startDate;
        private String convictionDate;
        private List<NowsOrderResult> results = new ArrayList<NowsOrderResult>();

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

        public Builder withResults(List<NowsOrderResult> results) {
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
