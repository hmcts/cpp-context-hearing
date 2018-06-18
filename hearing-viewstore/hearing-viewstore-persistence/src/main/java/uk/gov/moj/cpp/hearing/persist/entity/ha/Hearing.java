package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "ha_hearing")
public class Hearing {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private List<Defendant> defendants = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private List<Witness> witnesses = new ArrayList<>();

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "hearing_type")
    private String hearingType;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private List<HearingDate> hearingDays = new ArrayList<>();

    @Column(name = "court_centre_id")
    private UUID courtCentreId;

    @Column(name = "court_centre_name")
    private String courtCentreName;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "room_name")
    private String roomName;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private List<Attendee> attendees = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private List<ResultLine> resultLines = new ArrayList<>();

    public Hearing() {
    }

    public Hearing(Builder builder) {
        this.id = builder.id;
        this.hearingType = builder.hearingType;
        this.hearingDays = builder.hearingDays;
        this.courtCentreId = builder.courtCentreId;
        this.courtCentreName = builder.courtCentreName;
        this.roomId = builder.roomId;
        this.roomName = builder.roomName;
        this.setDefendants(builder.defendants);
        this.setWitnesses(builder.witnesses);
        this.hearingDays = builder.hearingDays;
        this.attendees = builder.attendees;
        if (builder.judgeBuilder != null) {
            this.attendees.add(builder.judgeBuilder.build());
        }
        this.resultLines = builder.resultLines;
    }

    public void setDefendants(List<Defendant> defendants) {
        this.defendants = defendants;
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public List<Witness> getWitnesses() {
        return witnesses;
    }

    public void setWitnesses(List<Witness> witnesses) {
        this.witnesses = witnesses;
    }

    public void setAttendees(List<Attendee> attendees) {
        this.attendees = attendees;
    }

    public List<Attendee> getAttendees() {
        return attendees;
    }

    public UUID getId() {
        return id;
    }

    public String getHearingType() {
        return hearingType;
    }

    public List<HearingDate> getHearingDays() {
        return hearingDays;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public List<ResultLine> getResultLines() {
        return resultLines;
    }

    public static class Builder {
        private UUID id;
        private List<Defendant> defendants = new ArrayList<>();
        private String hearingType;
        private List<HearingDate> hearingDays = new ArrayList<>();
        private UUID courtCentreId;
        private String courtCentreName;
        private UUID roomId;
        private String roomName;
        private Judge.Builder judgeBuilder;
        private List<Witness> witnesses = new ArrayList<>();
        private List<Attendee> attendees = new ArrayList<>();
        private List<ResultLine> resultLines = new ArrayList<>();

        protected Builder() {}
        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withDefendants(List<Defendant> defendants) {
            this.defendants = defendants;
            return this;
        }

        public Builder withWitnesses(List<Witness> witnesses){
            this.witnesses = witnesses;
            return this;
        }

        public Builder withJudge(Judge.Builder builder) {
            this.judgeBuilder = builder;
            return this;
        }

        public Builder withHearingType(String hearingType) {
            this.hearingType = hearingType;
            return this;
        }

        public Builder withHearingDays(List<HearingDate> hearingDays) {
            this.hearingDays = hearingDays;
            return this;
        }

        public Builder withCourtCentreId(UUID courtCentreId) {
            this.courtCentreId = courtCentreId;
            return this;
        }

        public Builder withCourtCentreName(String courtCentreName) {
            this.courtCentreName = courtCentreName;
            return this;
        }

        public Builder withRoomId(UUID roomId) {
            this.roomId = roomId;
            return this;
        }

        public Builder withRoomName(String roomName) {
            this.roomName = roomName;
            return this;
        }

        public Builder addAttendee(Attendee attendee){
            this.attendees.add(attendee);
            return this;
        }

        public Builder withResultLines(List<ResultLine> resultLines) {
            this.resultLines = resultLines;
            return this;
        }

        public Hearing build() {
            return new Hearing(this);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.id, ((Hearing)o).id);
    }
}