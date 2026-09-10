package uk.gov.moj.cpp.hearing.listing.domain.referencedata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JudiciariesList {

    private List<Judiciary> judiciaries;

    @JsonCreator
    public JudiciariesList(@JsonProperty("judiciaries") final List<Judiciary> judiciaries) {
        this.judiciaries = judiciaries;
    }

    public List<Judiciary> getJudiciaries() {
        return judiciaries;
    }

    public void setJudiciaries(final List<Judiciary> judiciaries) {
        this.judiciaries = judiciaries;
    }
}
