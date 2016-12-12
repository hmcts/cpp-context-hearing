package uk.gov.moj.cpp.hearing.persist.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name = "Hearing")
public class Hearing {

    @Id
    @Column(name = "hearingid", unique = true, nullable = false)
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

    public LocalDate getStartdate() {
        return startdate;
    }

    public void setStartdate(LocalDate startdate) {
        this.startdate = startdate;
    }

    public UUID geHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public void setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
    }

    public String getHearingType() {
        return hearingType;
    }

    public void setHearingType(String hearingType) {
        this.hearingType = hearingType;
    }

    public void setStartedAt(ZonedDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public ZonedDateTime getStartedAt() {
        return startedAt;
    }

    public ZonedDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(ZonedDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

}
