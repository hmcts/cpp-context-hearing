package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import java.util.List;

public final class HearingDetailsResponse {

    private final String hearingId;
    private final String startDate;
    private final String startTime;
    private final String roomName;
    private final String hearingType;
    private final String courtCentreName;
    private final Judge judge;
    private final String roomId;
    private final String courtCentreId;
    private final Attendees attendees;
    private final List<Case> cases;

    public HearingDetailsResponse() {
        this.hearingId = null;
        this.startDate = null;
        this.startTime = null;
        this.roomName = null;
        this.hearingType = null;
        this.courtCentreName = null;
        this.judge = null;
        this.roomId = null;
        this.courtCentreId = null;
        this.attendees = null;
        this.cases = null;
    }

    private HearingDetailsResponse(final Builder builder) {
        this.hearingId = builder.hearingId;
        this.startDate = builder.startDate;
        this.startTime = builder.startTime;
        this.roomName = builder.roomName;
        this.hearingType = builder.hearingType;
        this.courtCentreName = builder.courtCentreName;
        this.judge = builder.judge;
        this.roomId = builder.roomId;
        this.courtCentreId = builder.courtCentreId;
        this.attendees = builder.attendees;
        this.cases = builder.cases;
    }

    public String getHearingId() {
        return hearingId;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getHearingType() {
        return hearingType;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public Judge getJudge() {
        return judge;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getCourtCentreId() {
        return courtCentreId;
    }

    public Attendees getAttendees() {
        return attendees;
    }

    public List<Case> getCases() {
        return cases;
    }

    public static Builder builder() {
        return new Builder();
    }
    public static final class Builder {

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
        private List<Case> cases;
        
        public Builder withHearingId(final String hearingId) {
            this.hearingId = hearingId;
            return this;
        }
        
        public Builder withStartDate(final String startDate) {
            this.startDate = startDate;
            return this;
        }
        
        public Builder withStartTime(final String startTime) {
            this.startTime = startTime;
            return this;
        }
        
        public Builder withRoomName(final String roomName) {
            this.roomName = roomName;
            return this;
        }
        
        public Builder withHearingType(final String hearingType) {
            this.hearingType = hearingType;
            return this;
        }
        
        public Builder withCourtCentreName(final String courtCentreName) {
            this.courtCentreName = courtCentreName;
            return this;
        }
        
        public Builder withJudge(final Judge judge) {
            this.judge = judge;
            return this;
        }
        
        public Builder withRoomId(final String roomId) {
            this.roomId = roomId;
            return this;
        }
        
        public Builder withCourtCentreId(final String courtCentreId) {
            this.courtCentreId = courtCentreId;
            return this;
        }
        
        public Builder withAttendees(final Attendees attendees) {
            this.attendees = attendees;
            return this;
        }
        
        public Builder withCases(final List<Case> cases) {
            this.cases = cases;
            return this;
        }
        
        public HearingDetailsResponse build() {
            return new HearingDetailsResponse(this);
        }
    }
}