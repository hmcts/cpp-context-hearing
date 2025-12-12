package uk.gov.moj.cpp.hearing.eventlog;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CourtCentre {
    private UUID courtCentreId;
    private String courtCentreName;
    private UUID courtRoomId;
    private String courtRoomName;

    @JsonCreator
    public CourtCentre(@JsonProperty("courtCentreId") final UUID courtCentreId,
                       @JsonProperty("courtCentreName") final String courtCentreName,
                       @JsonProperty("courtRoomId") final UUID courtRoomId,
                       @JsonProperty("courtRoomName") final String courtRoomName) {
        this.courtCentreId = courtCentreId;
        this.courtCentreName = courtCentreName;
        this.courtRoomId = courtRoomId;
        this.courtRoomName = courtRoomName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public UUID getCourtRoomId() {
        return courtRoomId;
    }

    public String getCourtRoomName() {
        return courtRoomName;
    }

    public static class Builder {
        private UUID courtCentreId;
        private String courtCentreName;
        private UUID courtRoomId;
        private String courtRoomName;

        public Builder withCourtCentreId(UUID courtCentreId) {
            this.courtCentreId = courtCentreId;
            return this;
        }

        public Builder withCourtCentreName(String courtCentreName) {
            this.courtCentreName = courtCentreName;
            return this;
        }

        public Builder withCourtRoomId(UUID courtRoomId) {
            this.courtRoomId = courtRoomId;
            return this;
        }

        public Builder withCourtRoomName(String courtRoomName) {
            this.courtRoomName = courtRoomName;
            return this;
        }

        public CourtCentre build() {
            return new CourtCentre(this.courtCentreId, this.courtCentreName, this.courtRoomId, this.courtRoomName);
        }
    }
}