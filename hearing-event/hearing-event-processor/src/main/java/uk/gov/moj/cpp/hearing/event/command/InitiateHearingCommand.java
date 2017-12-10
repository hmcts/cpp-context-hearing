package uk.gov.moj.cpp.hearing.event.command;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public class InitiateHearingCommand implements Serializable {

    private static final long serialVersionUID = -2695043732753714603L;

    private UUID hearingId;
    private UUID caseId;
    private String hearingType;
    private ZonedDateTime startDateTime;
    private Integer duration;
    private UUID courtCentreId;
    private UUID roomId;
    private String courtCentreName;
    private String roomName;

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public void setCourtCentreId(final UUID courtCentreId) {
        this.courtCentreId = courtCentreId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(final UUID roomId) {
        this.roomId = roomId;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public void setCourtCentreName(final String courtCentreName) {
        this.courtCentreName = courtCentreName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(final String roomName) {
        this.roomName = roomName;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public void setCaseId(final UUID caseId) {
        this.caseId = caseId;
    }

    public void setHearingType(final String hearingType) {
        this.hearingType = hearingType;
    }

    public void setStartDateTime(final ZonedDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setDuration(final Integer duration) {
        this.duration = duration;
    }

    public Integer getDuration() {
        return duration;
    }


    public String getHearingType() {
        return hearingType;
    }

    public ZonedDateTime getStartDateTime() {
        return startDateTime;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    @Override
    public String toString() {
        return "InitiateHearingCommand [hearingId=" + hearingId.toString() + ", caseId=" + caseId + ", hearingType=" + hearingType
                        + ", startDateTime=" + startDateTime + ", duration=" + duration
                        + "courtCentreId= " + courtCentreId.toString() + "courtCentreName= "
                        + courtCentreName + " roomId=" + roomId.toString() + "roomName=" + roomName
                        + "]";
    }

}
