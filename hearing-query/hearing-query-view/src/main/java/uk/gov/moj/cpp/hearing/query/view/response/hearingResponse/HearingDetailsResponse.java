
package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "hearingId",
        "startDate",
        "startTime",
        "roomName",
        "hearingType",
        "courtCentreName",
        "judge",
        "roomId",
        "courtCentreId",
        "attendees",
        "cases"
})
public class HearingDetailsResponse {
    private String hearingId;
    private String startDate;
    private String startTime;
    private String roomName;
    private String hearingType;
    private String courtCentreName;
    private Judge judge;
    private String roomId;
    private String courtCentreId;
    private Attendees attendees;
    private List<Case> cases = null;

    public String getHearingId() {
        return hearingId;
    }

    public void setHearingId(String hearingId) {
        this.hearingId = hearingId;
    }

    public HearingDetailsResponse withHearingId(String hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public HearingDetailsResponse withStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public HearingDetailsResponse withStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public HearingDetailsResponse withRoomName(String roomName) {
        this.roomName = roomName;
        return this;
    }

    public String getHearingType() {
        return hearingType;
    }

    public void setHearingType(String hearingType) {
        this.hearingType = hearingType;
    }

    public HearingDetailsResponse withHearingType(String hearingType) {
        this.hearingType = hearingType;
        return this;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public void setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
    }

    public HearingDetailsResponse withCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
        return this;
    }

    public Judge getJudge() {
        return judge;
    }

    public void setJudge(Judge judge) {
        this.judge = judge;
    }

    public HearingDetailsResponse withJudge(Judge judge) {
        this.judge = judge;
        return this;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public HearingDetailsResponse withRoomId(String roomId) {
        this.roomId = roomId;
        return this;
    }

    public String getCourtCentreId() {
        return courtCentreId;
    }

    public void setCourtCentreId(String courtCentreId) {
        this.courtCentreId = courtCentreId;
    }

    public HearingDetailsResponse withCourtCentreId(String courtCentreId) {
        this.courtCentreId = courtCentreId;
        return this;
    }

    public Attendees getAttendees() {
        return attendees;
    }

    public void setAttendees(Attendees attendees) {
        this.attendees = attendees;
    }

    public HearingDetailsResponse withAttendees(Attendees attendees) {
        this.attendees = attendees;
        return this;
    }

    public List<Case> getCases() {
        return cases;
    }

    public void setCases(List<Case> cases) {
        this.cases = cases;
    }

    public HearingDetailsResponse withCases(List<Case> cases) {
        this.cases = cases;
        return this;
    }

}