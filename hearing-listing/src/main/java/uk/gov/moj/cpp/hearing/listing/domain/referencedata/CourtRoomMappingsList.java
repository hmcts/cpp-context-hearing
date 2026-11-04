package uk.gov.moj.cpp.hearing.listing.domain.referencedata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CourtRoomMappingsList {

    private List<CourtRoomMapping> cpXhibitCourtRoomMappings;

    @JsonCreator
    public CourtRoomMappingsList(final List<CourtRoomMapping> cpXhibitCourtRoomMappings) {
        this.cpXhibitCourtRoomMappings = cpXhibitCourtRoomMappings;
    }

    public List<CourtRoomMapping> getCpXhibitCourtRoomMappings() {
        return cpXhibitCourtRoomMappings;
    }

    public void setCpXhibitCourtRoomMappings(final List<CourtRoomMapping> cpXhibitCourtRoomMappings) {
        this.cpXhibitCourtRoomMappings = cpXhibitCourtRoomMappings;
    }
}