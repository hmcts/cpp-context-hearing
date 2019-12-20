package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit;

import uk.gov.justice.core.courts.HearingEvent;

import java.io.Serializable;

public class CourtRoom implements Serializable {
    private static final long serialVersionUID = -4151921355339340656L;

    private String courtRoomName;

    private Cases cases;

    private HearingEvent hearingEvent;

    public CourtRoom(final String courtRoomName, final Cases cases, final HearingEvent hearingEvent) {
        this.courtRoomName = courtRoomName;
        this.cases = cases;
        this.hearingEvent = hearingEvent;
    }

    public String getCourtRoomName() {
        return courtRoomName;
    }

    public Cases getCases() {
        return cases;
    }

    public HearingEvent getHearingEvent() {
        return hearingEvent;
    }

    public static Builder courtRoom() {
        return new CourtRoom.Builder();
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CourtRoom that = (CourtRoom) obj;

        return java.util.Objects.equals(this.courtRoomName, that.courtRoomName) &&
                java.util.Objects.equals(this.hearingEvent, that.hearingEvent) &&
                java.util.Objects.equals(this.cases, that.cases);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(courtRoomName, cases);
    }

    @Override
    public String toString() {
        return "CourtRoom{" +
                "courtRoomName='" + courtRoomName + "'," +
                "hearingEvent='" + hearingEvent + "'," +
                "cases='" + cases + "'" +
                "}";
    }

    public CourtRoom setCourtRoomName(String courtRoomName) {
        this.courtRoomName = courtRoomName;
        return this;
    }

    public CourtRoom setCases(Cases cases) {
        this.cases = cases;
        return this;
    }

    public CourtRoom setHearingEvent(HearingEvent hearingEvent) {
        this.hearingEvent = hearingEvent;
        return this;
    }

    public static class Builder {
        private String courtRoomName;

        private Cases cases;

        private HearingEvent hearingEvent;

        public Builder withCourtRoomName(final String courtRoomName) {
            this.courtRoomName = courtRoomName;
            return this;
        }

        public Builder withCases(final Cases cases) {
            this.cases = cases;
            return this;
        }

        public Builder withHearingEvent(final HearingEvent hearingEvent) {
            this.hearingEvent = hearingEvent;
            return this;
        }

        public CourtRoom build() {
            return new CourtRoom(courtRoomName, cases, hearingEvent);
        }
    }
}
