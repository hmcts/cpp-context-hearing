package uk.gov.moj.cpp.hearing.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CourtRoom {

    private final String courtRoomName;
    private List<DefendantDetail> defendantDetails = new ArrayList<>();

    @JsonCreator
    public CourtRoom(@JsonProperty("courtRoomName") final String courtRoomName,
                     @JsonProperty("defendantDetails") final List<DefendantDetail> defendantDetails) {
        this.courtRoomName = courtRoomName;
        this.defendantDetails = defendantDetails;
    }

    public static Builder courtRoom() {
        return new Builder();
    }

    public String getCourtRoomName() {
        return courtRoomName;
    }

    public List<DefendantDetail> getDefendantDetails() {
        return defendantDetails;
    }

    public static class Builder {

        private String courtRoomName;
        private List<DefendantDetail> defendantDetails = null;


        public CourtRoom.Builder withCourtRoomName(final String courtRoomName) {
            this.courtRoomName = courtRoomName;
            return this;
        }

        public CourtRoom.Builder withDefendantDetails(final List<DefendantDetail> defendantDetails) {
            this.defendantDetails = defendantDetails;
            return this;
        }

        public CourtRoom build() {
            return new CourtRoom(courtRoomName, defendantDetails);
        }
    }

}