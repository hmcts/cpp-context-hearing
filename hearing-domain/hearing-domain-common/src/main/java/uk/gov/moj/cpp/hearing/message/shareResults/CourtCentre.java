package uk.gov.moj.cpp.hearing.message.shareResults;

import java.util.UUID;

public class CourtCentre {

    private UUID courtCentreId;
    private String courtCentreName;
    private UUID courtRoomId;
    private String courtRoomName;

    public static CourtCentre courtCentre() {
        return new CourtCentre();
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public CourtCentre setCourtCentreId(UUID courtCentreId) {
        this.courtCentreId = courtCentreId;
        return this;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public CourtCentre setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
        return this;
    }

    public UUID getCourtRoomId() {
        return courtRoomId;
    }

    public CourtCentre setCourtRoomId(UUID courtRoomId) {
        this.courtRoomId = courtRoomId;
        return this;
    }

    public String getCourtRoomName() {
        return courtRoomName;
    }

    public CourtCentre setCourtRoomName(String courtRoomName) {
        this.courtRoomName = courtRoomName;
        return this;
    }
}
