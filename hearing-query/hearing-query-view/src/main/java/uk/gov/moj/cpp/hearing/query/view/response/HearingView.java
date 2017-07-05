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


    public String getHearingId() {
        return hearingId;
    }

    public void setHearingId(String hearingId) {
        this.hearingId = hearingId;
    }


    public HearingView() {
        super();
    }


    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getHearingType() {
        return hearingType;
    }

    public void setHearingType(String hearingType) {
        this.hearingType = hearingType;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public void setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public List<String> getCaseIds() {
        return caseIds;
    }

    public void setCaseIds(List<String> caseIds) {
        this.caseIds = caseIds;
    }
}
