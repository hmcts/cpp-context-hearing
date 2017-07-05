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

    @Column(name = "roomname")
    private String roomName;

    @Column(name = "hearingtype")
    private String hearingType;

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

    public Builder builder() {
        return new Builder(getHearingId(), getStartDate(), getStartTime(), getDuration(), getRoomName(),
                getHearingType(), getCourtCentreName());
    }

    public class Builder {
        private final UUID hearingId;
        private LocalDate startDate;
        private LocalTime startTime;
        private Integer duration;
        private String roomName;
        private String hearingType;
        private String courtCentreName;

        public Builder(final UUID hearingId, final LocalDate startDate, final LocalTime startTime,
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

        public Hearing build() {
            return new Hearing(hearingId, startDate, startTime, duration, roomName, hearingType,
                    courtCentreName);
        }
    }

}
