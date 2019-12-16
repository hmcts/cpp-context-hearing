package uk.gov.moj.cpp.external.domain.referencedata;

import java.util.List;

public class CourtRoomMappingsList {

   private List<CourtRoomMappings> cpXhibitCourtRoomMappings;

    public CourtRoomMappingsList(final List<CourtRoomMappings> cpXhibitCourtRoomMappings) {
        this.cpXhibitCourtRoomMappings = cpXhibitCourtRoomMappings;
    }

    public List<CourtRoomMappings> getCpXhibitCourtRoomMappings() {
        return cpXhibitCourtRoomMappings;
    }
}
