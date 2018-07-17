package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import uk.gov.moj.cpp.hearing.command.witness.DefenceWitness;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HearingDetailsResponse {

    private final String hearingId;
    private final String startDate;
    private final String startTime;
    private final List<String> hearingDays;
    private final String roomName;
    private final String hearingType;
    private final String courtCentreName;
    private final Judge judge;
    private final String roomId;
    private final String courtCentreId;
    private final Attendees attendees;
    private final List<Case> cases;
    private final List<DefenceWitness> defenceWitnesses;
    private final List<ResultLine> resultLines;

    public HearingDetailsResponse() {
        this.hearingId = null;
        this.startDate = null;
        this.startTime = null;
        this.roomName = null;
        this.hearingType = null;
        this.hearingDays = null;
        this.courtCentreName = null;
        this.judge = null;
        this.roomId = null;
        this.courtCentreId = null;
        this.attendees = null;
        this.cases = null;
        this.defenceWitnesses = null;
        this.resultLines = null;
    }

    @JsonCreator
    private HearingDetailsResponse(@JsonProperty("hearingId") final String hearingId,
                                   @JsonProperty("startDate") final String startDate,
                                   @JsonProperty("startTime") final String startTime,
                                   @JsonProperty("hearingDays") final List<String> hearingDays,
                                   @JsonProperty("roomName") final String roomName,
                                   @JsonProperty("hearingType") final String hearingType,
                                   @JsonProperty("courtCentreName") final String courtCentreName,
                                   @JsonProperty("judge") final Judge judge,
                                   @JsonProperty("roomId") final String roomId,
                                   @JsonProperty("courtCentreId") final String courtCentreId,
                                   @JsonProperty("attendees") final Attendees attendees,
                                   @JsonProperty("cases") final List<Case> cases,
                                   @JsonProperty("defenceWitnesses") final List<DefenceWitness> defenceWitnesses,
                                   @JsonProperty("resultLines") final List<ResultLine> resultLines) {
        this.hearingId = hearingId;
        this.startDate = startDate;
        this.startTime = startTime;
        this.hearingDays = hearingDays;
        this.roomName = roomName;
        this.hearingType = hearingType;
        this.courtCentreName = courtCentreName;
        this.judge = judge;
        this.roomId = roomId;
        this.courtCentreId = courtCentreId;
        this.attendees = attendees;
        this.cases = cases;
        this.defenceWitnesses = defenceWitnesses;
        this.resultLines = resultLines;
    }

    private HearingDetailsResponse(final Builder builder) {
        this.hearingId = builder.hearingId;
        this.startDate = builder.startDate;
        this.startTime = builder.startTime;
        this.hearingDays = builder.hearingDays;
        this.roomName = builder.roomName;
        this.hearingType = builder.hearingType;
        this.courtCentreName = builder.courtCentreName;
        this.judge = builder.judge;
        this.roomId = builder.roomId;
        this.courtCentreId = builder.courtCentreId;
        this.attendees = builder.attendees;
        this.cases = builder.cases;
        this.defenceWitnesses = builder.defenceWitnesses;
        this.resultLines = builder.resultLines;
    }

    public static Builder builder() {
        return new Builder();
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

    public List<String> getHearingDays() {
        return hearingDays;
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

    public List<DefenceWitness> getDefenceWitnesses() {
        return defenceWitnesses;
    }

    public List<ResultLine> getResultLines() {
        return resultLines;
    }

    public static final class Builder {

        private String hearingId;
        private String startDate;
        private String startTime;
        private List<String> hearingDays;
        private String roomName;
        private String hearingType;
        private String courtCentreName;
        private Judge judge;
        private String roomId;
        private String courtCentreId;
        private Attendees attendees;
        private List<Case> cases;
        private List<DefenceWitness> defenceWitnesses;
        private List<ResultLine> resultLines;

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

        public Builder withHearingDays(final List<String> hearingDays) {
            this.hearingDays = hearingDays;
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

        public Builder withDefenceWiteness(final List<DefenceWitness> defenceWitnesses) {
            this.defenceWitnesses = defenceWitnesses;
            return this;
        }

        public Builder withResultLines(final List<ResultLine> resultLines) {
            this.resultLines = resultLines;
            return this;
        }

        public HearingDetailsResponse build() {
            return new HearingDetailsResponse(this);
        }
    }
}