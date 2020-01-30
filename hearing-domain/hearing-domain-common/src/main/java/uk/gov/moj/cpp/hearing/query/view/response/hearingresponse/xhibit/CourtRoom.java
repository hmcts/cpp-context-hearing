package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.HearingEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CourtRoom implements Serializable {
    private static final long serialVersionUID = -4151921355339340656L;

    private UUID courtRoomId;

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

    @SuppressWarnings("squid:AssignmentInSubExpressionCheck")
    public List<UUID> getLinkedCaseIds() {
        return null == linkedCaseIds ? linkedCaseIds = new ArrayList<>(): linkedCaseIds;
    }

    public void setCourtRoomId(final UUID courtRoomId){
        this.courtRoomId = courtRoomId;

    }



    public static Builder courtRoom() {
        return new CourtRoom.Builder();
    }

    @SuppressWarnings({"squid:S00121", "squid:S00122"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CourtRoom courtRoom = (CourtRoom) o;

        return courtRoomId.equals(courtRoom.courtRoomId);
    }

    @Override
    public int hashCode() {
        return courtRoomId.hashCode();
    }

    public UUID getCourtRoomId() {

        return courtRoomId;
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
