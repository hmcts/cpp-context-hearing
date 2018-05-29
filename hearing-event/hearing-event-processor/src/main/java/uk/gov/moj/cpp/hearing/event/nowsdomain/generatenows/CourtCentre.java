package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class CourtCentre {

    private String courtCentreId;

    private String courtCentreName;

    private String courtRoomId;

    private String courtRoomName;

    public static CourtCentre courtCentre() {
        return new CourtCentre();
    }

    public String getCourtCentreId() {
        return this.courtCentreId;
    }

    public CourtCentre setCourtCentreId(String courtCentreId) {
        this.courtCentreId = courtCentreId;
        return this;
    }

    public String getCourtCentreName() {
        return this.courtCentreName;
    }

    public CourtCentre setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
        return this;
    }

    public String getCourtRoomId() {
        return this.courtRoomId;
    }

    public CourtCentre setCourtRoomId(String courtRoomId) {
        this.courtRoomId = courtRoomId;
        return this;
    }

    public String getCourtRoomName() {
        return this.courtRoomName;
    }

    public CourtCentre setCourtRoomName(String courtRoomName) {
        this.courtRoomName = courtRoomName;
        return this;
    }
}
