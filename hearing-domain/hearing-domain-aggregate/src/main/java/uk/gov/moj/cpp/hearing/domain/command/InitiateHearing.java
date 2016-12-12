package uk.gov.moj.cpp.hearing.domain.command;

import java.time.ZonedDateTime;
import java.util.UUID;

public class InitiateHearing {

    private UUID hearingId;

    private ZonedDateTime startDateTime;

    private Integer duration;

    private String courtCentreName;

    private String roomName;

    private UUID caseId;

    private String hearingType;

    public InitiateHearing(UUID hearingId, ZonedDateTime startDateTime, Integer duration,String hearingType) {
        super();
        this.hearingId = hearingId;
        this.startDateTime = startDateTime;
        this.duration = duration;
        this.hearingType =hearingType;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ZonedDateTime getStartDateTime() {
        return startDateTime;
    }

    public Integer getDuration() {
        return duration;
    }


    public String getCourtCentreName() {
        return courtCentreName;
    }

    public void setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public void setCaseId(UUID caseId) {
        this.caseId = caseId;
    }

    public String getHearingType() {
        return hearingType;
    }

    public void setHearingType(String hearingType) {
        this.hearingType = hearingType;
    }
}
