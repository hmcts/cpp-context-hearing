package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.courtcentre;

import java.util.List;

public class CourtCentreCourtList {

    private List<CourtCentreCode> cpXhibitCourtMappings;

    public CourtCentreCourtList(final List<CourtCentreCode> cpXhibitCourtMappings) {
        this.cpXhibitCourtMappings = cpXhibitCourtMappings;
    }

    public List<CourtCentreCode> getCpXhibitCourtMappings() {
        return cpXhibitCourtMappings;
    }
}
