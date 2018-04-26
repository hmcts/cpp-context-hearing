package uk.gov.moj.cpp.hearing.message.shareResults;

import java.util.UUID;

public class CourtCentre {

    private UUID courtCentreId;
    private String courtCentreName;
    private UUID courtRoomId;
    private String courtRoomName;

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

    public CourtCentre setCourtCentreId(UUID courtCentreId) {
        this.courtCentreId = courtCentreId;
        return this;
    }

    public CourtCentre setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
        return this;
    }

    public CourtCentre setCourtRoomId(UUID courtRoomId) {
        this.courtRoomId = courtRoomId;
        return this;
    }

    public CourtCentre setCourtRoomName(String courtRoomName) {
        this.courtRoomName = courtRoomName;
        return this;
    }

    public static CourtCentre courtCentre() {
        return new CourtCentre();
    }
}
