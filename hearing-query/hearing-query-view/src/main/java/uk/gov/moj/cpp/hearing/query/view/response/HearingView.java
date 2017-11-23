package uk.gov.moj.cpp.hearing.query.view.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class HearingView {

    private String hearingId;

    private LocalDate startDate;

    private LocalTime startTime;

    private Integer duration;

    private String roomName;

    private String hearingType;

    private String courtCentreName;

    private List<String> caseIds;

    private String roomId;

    private String courtCentreId;


    public String getHearingId() {
        return hearingId;
    }

    public void setHearingId(final String hearingId) {
        this.hearingId = hearingId;
    }


    public HearingView() {
        super();
    }


    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(final Integer duration) {
        this.duration = duration;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(final String roomName) {
        this.roomName = roomName;
    }

    public String getHearingType() {
        return hearingType;
    }

    public void setHearingType(final String hearingType) {
        this.hearingType = hearingType;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public void setCourtCentreName(final String courtCentreName) {
        this.courtCentreName = courtCentreName;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(final LocalTime startTime) {
        this.startTime = startTime;
    }

    public List<String> getCaseIds() {
        return caseIds;
    }

    public void setCaseIds(final List<String> caseIds) {
        this.caseIds = caseIds;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(final String roomId) {
        this.roomId = roomId;
    }

    public String getCourtCentreId() {
        return courtCentreId;
    }

    public void setCourtCentreId(final String courtCentreId) {
        this.courtCentreId = courtCentreId;
    }
}
