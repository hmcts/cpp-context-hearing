package uk.gov.moj.cpp.hearing.persist.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "hearing")
public class Hearing {

    @Id
    @Column(name = "hearingid", nullable = false)
    private UUID hearingId;

    @Column(name = "startdate")
    private LocalDate startDate;

    @Column(name = "starttime")
    private LocalTime startTime;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "roomid")
    private UUID roomId;

    @Column(name = "roomname")
    private String roomName;

    @Column(name = "hearingtype")
    private String hearingType;

    @Column(name = "courtcentreid")
    private UUID courtCentreId;

    @Column(name = "courtcentrename")
    private String courtCentreName;

    public Hearing() {
        // for JPA
    }

    public Hearing(final UUID hearingId, final LocalDate startDate, final LocalTime startTime,
                   final Integer duration, final String roomName, final String hearingType,
                   final String courtCentreName) {
        this.hearingId = hearingId;
        this.startDate = startDate;
        this.startTime = startTime;
        this.duration = duration;
        this.roomName = roomName;
        this.hearingType = hearingType;
        this.courtCentreName = courtCentreName;
    }

    private Hearing(final Builder builder) {
        this.hearingId = builder.hearingId;
        this.startDate = builder.startDate;
        this.startTime = builder.startTime;
        this.duration = builder.duration;
        this.roomId = builder.roomId;
        this.roomName = builder.roomName;
        this.hearingType = builder.hearingType;
        this.courtCentreId = builder.courtCentreId;
        this.courtCentreName = builder.courtCentreName;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public Integer getDuration() {
        return duration;
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

    public UUID getRoomId() {
        return roomId;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public Builder builder() {
        // Ensure existing values are retained untill builder overrides it
        return new Builder().withHearingId(getHearingId()).withStartTime(getStartTime())
                .withStartDate(getStartDate()).withDuration(getDuration())
                .withRoomId(getRoomId()).withRoomName(roomName)
                .withHearingType(getHearingType())
                .withCourtCentreId(getCourtCentreId()).withCourtCentreName(courtCentreName);
    }

    public static class Builder {
        private UUID hearingId;
        private LocalDate startDate;
        private LocalTime startTime;
        private Integer duration;
        private UUID roomId;
        private String roomName;
        private String hearingType;
        private UUID courtCentreId;
        private String courtCentreName;

        public Builder withStartDate(final LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder withStartTime(final LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder withDuration(final Integer duration) {
            this.duration = duration;
            return this;
        }

        public Builder withRoomId(final UUID roomId) {
            this.roomId = roomId;
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

        public Builder withCourtCentreId(final UUID courtCentreId) {
            this.courtCentreId = courtCentreId;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Hearing build() {
            return new Hearing(this);
        }
    }

}
