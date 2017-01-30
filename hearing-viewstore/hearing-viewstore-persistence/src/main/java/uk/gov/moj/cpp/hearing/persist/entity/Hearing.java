package uk.gov.moj.cpp.hearing.persist.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
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
    private LocalDate startdate;

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

    @Column(name = "started_at")
    private ZonedDateTime startedAt;

    @Column(name = "ended_at")
    private ZonedDateTime endedAt;

    public Hearing() {
        // for JPA //NOSONAR
    }

    public Hearing(final UUID hearingId, final LocalDate startdate, final LocalTime startTime,
                   final Integer duration, final String roomName, final String hearingType,
                   final String courtCentreName, final ZonedDateTime startedAt,
                   final ZonedDateTime endedAt) {
        this.hearingId = hearingId;
        this.startdate = startdate;
        this.startTime = startTime;
        this.duration = duration;
        this.roomName = roomName;
        this.hearingType = hearingType;
        this.courtCentreName = courtCentreName;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public LocalDate getStartdate() {
        return startdate;
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

    public ZonedDateTime getStartedAt() {
        return startedAt;
    }

    public ZonedDateTime getEndedAt() {
        return endedAt;
    }

    public Builder builder() {
        return new Builder(getHearingId(), getStartdate(), getStartTime(), getDuration(), getRoomName(),
                getHearingType(), getCourtCentreName(), getStartedAt(), getEndedAt());
    }

    public class Builder {
        private final UUID hearingId;
        private LocalDate startdate;
        private LocalTime startTime;
        private Integer duration;
        private String roomName;
        private String hearingType;
        private String courtCentreName;
        private ZonedDateTime startedAt;
        private ZonedDateTime endedAt;

        public Builder(final UUID hearingId, final LocalDate startdate, final LocalTime startTime,
                        final Integer duration, final String roomName, final String hearingType,
                        final String courtCentreName, final ZonedDateTime startedAt,
                        final ZonedDateTime endedAt) {
            this.hearingId = hearingId;
            this.startdate = startdate;
            this.startTime = startTime;
            this.duration = duration;
            this.roomName = roomName;
            this.hearingType = hearingType;
            this.courtCentreName = courtCentreName;
            this.startedAt = startedAt;
            this.endedAt = endedAt;
        }

        public Builder withStartdate(final LocalDate startdate) {
            this.startdate = startdate;
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

        public Builder withStartedAt(final ZonedDateTime startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder withEndedAt(final ZonedDateTime endedAt) {
            this.endedAt = endedAt;
            return this;
        }

        public Hearing build() {
            return new Hearing(hearingId, startdate, startTime, duration, roomName, hearingType,
                    courtCentreName, startedAt, endedAt);
        }
    }

}
