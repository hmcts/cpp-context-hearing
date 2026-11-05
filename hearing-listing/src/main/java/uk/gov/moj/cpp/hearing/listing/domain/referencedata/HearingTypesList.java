package uk.gov.moj.cpp.hearing.listing.domain.referencedata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HearingTypesList {

    private List<HearingType> hearingTypes;

    @JsonCreator
    public HearingTypesList(@JsonProperty("hearingTypes") final List<HearingType> hearingTypes) {
        this.hearingTypes = hearingTypes;
    }

    public List<HearingType> getHearingTypes() {
        return hearingTypes;
    }

    public void setHearingTypes(final List<HearingType> hearingTypes) {
        this.hearingTypes = hearingTypes;
    }
}
