package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;


public class CourtCentre implements Serializable {
    private final static long serialVersionUID = 7891300505006355702L;

    private String courtCentreId;
    private String courtCentreName;
    private String courtRoomId;
    private String courtRoomName;

    public String getCourtCentreId() {
        return courtCentreId;
    }

    public void setCourtCentreId(String courtCentreId) {
        this.courtCentreId = courtCentreId;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public void setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
    }

    public String getCourtRoomId() {
        return courtRoomId;
    }

    public void setCourtRoomId(String courtRoomId) {
        this.courtRoomId = courtRoomId;
    }

    public String getCourtRoomName() {
        return courtRoomName;
    }

    public void setCourtRoomName(String courtRoomName) {
        this.courtRoomName = courtRoomName;
    }

    public static final class CourtCentreBuilder {
        private String courtCentreId;
        private String courtCentreName;
        private String courtRoomId;
        private String courtRoomName;

        private CourtCentreBuilder() {
        }

        public static CourtCentreBuilder builder() {
            return new CourtCentreBuilder();
        }

        public CourtCentreBuilder withCourtCentreId(String courtCentreId) {
            this.courtCentreId = courtCentreId;
            return this;
        }

        public CourtCentreBuilder withCourtCentreName(String courtCentreName) {
            this.courtCentreName = courtCentreName;
            return this;
        }

        public CourtCentreBuilder withCourtRoomId(String courtRoomId) {
            this.courtRoomId = courtRoomId;
            return this;
        }

        public CourtCentreBuilder withCourtRoomName(String courtRoomName) {
            this.courtRoomName = courtRoomName;
            return this;
        }

        public CourtCentre build() {
            CourtCentre courtCentre = new CourtCentre();
            courtCentre.setCourtCentreId(courtCentreId);
            courtCentre.setCourtCentreName(courtCentreName);
            courtCentre.setCourtRoomId(courtRoomId);
            courtCentre.setCourtRoomName(courtRoomName);
            return courtCentre;
        }
    }
}
