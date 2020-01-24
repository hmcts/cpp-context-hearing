package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.HearingEvent;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CourtRoom implements Serializable {
    private static final long serialVersionUID = -4151921355339340656L;

    private String courtRoomName;

    private Cases cases;

    private HearingEvent hearingEvent;

    private DefenceCounsel defenceCounsel;

    private List<UUID> linkedCaseIds;

    public CourtRoom(final String courtRoomName, final Cases cases,
                     final HearingEvent hearingEvent, final DefenceCounsel defenceCounsel,
                     final List<UUID> linkedCaseIds) {
        this.courtRoomName = courtRoomName;
        this.cases = cases;
        this.hearingEvent = hearingEvent;
        this.defenceCounsel = defenceCounsel;
        this.linkedCaseIds = linkedCaseIds;
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

    public DefenceCounsel getDefenceCounsel() {
        return defenceCounsel;
    }

    public List<UUID> getLinkedCaseIds() {
        return linkedCaseIds;
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
        return java.util.Objects.hash(courtRoomName, hearingEvent, cases);
    }

    @Override
    public String toString() {
        return "CourtRoom{" +
                "courtRoomName='" + courtRoomName + "'," +
                "hearingEvent='" + hearingEvent + "'," +
                "cases='" + cases + "'" +
                "defenceCounsel='" + defenceCounsel + "'" +
                "linkedCaseIds='" + linkedCaseIds + "'" +
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

        private List<DefenceCounsel> defenceCounsels;

        private List<UUID> linkedCaseIds;

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
            return new CourtRoom(courtRoomName, cases, hearingEvent, getDefenceCounsel(), linkedCaseIds);
        }

        private DefenceCounsel getDefenceCounsel() {
            return Optional.ofNullable(defenceCounsels).orElse(Collections.emptyList()).stream()
                    .filter(defenceCounsel -> hearingEvent != null &&
                            defenceCounsel.getId().equals(hearingEvent.getDefenceCounselId()))
                    .findFirst().orElse(null);
        }

        public Builder withDefenceCouncil(final List<DefenceCounsel> defenceCounsels) {
            this.defenceCounsels = defenceCounsels;
            return this;
        }

        public Builder withLinkedCaseIds(final List<UUID> linkedCaseIds) {
            this.linkedCaseIds = linkedCaseIds;
            return this;
        }
    }
}
