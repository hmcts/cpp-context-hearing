package uk.gov.moj.cpp.hearing.listing.domain.referencedata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CourtMappingsList {

    private List<CourtMapping> cpXhibitCourtMappings;

    @JsonCreator
    public CourtMappingsList(final List<CourtMapping> cpXhibitCourtMappings) {
        this.cpXhibitCourtMappings = cpXhibitCourtMappings;
    }

    public List<CourtMapping> getCpXhibitCourtMappings() {
        return cpXhibitCourtMappings;
    }
}
