package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.util.UUID;

public class CourtCentre implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID courtCentreId;

    private String courtCentreName;

    private UUID courtRoomId;

    private String courtRoomName;

    public static CourtCentre courtCentre() {
        return new CourtCentre();
    }

    public UUID getCourtCentreId() {
        return this.courtCentreId;
    }

    public CourtCentre setCourtCentreId(UUID courtCentreId) {
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

    public UUID getCourtRoomId() {
        return this.courtRoomId;
    }

    public CourtCentre setCourtRoomId(UUID courtRoomId) {
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
