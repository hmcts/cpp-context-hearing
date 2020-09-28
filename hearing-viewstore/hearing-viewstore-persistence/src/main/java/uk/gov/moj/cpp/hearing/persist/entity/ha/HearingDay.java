package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ha_hearing_day")
public class HearingDay {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Hearing hearing;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "date_time")
    private ZonedDateTime dateTime;

    @Column(name = "sitting_day")
    private ZonedDateTime sittingDay;

    @Column(name = "listing_sequence")
    private Integer listingSequence;

    @Column(name = "listed_duration_minutes")
    private Integer listedDurationMinutes;

    @Column(name = "is_cancelled")
    private Boolean isCancelled;

    //maskedOffences need to add

    public HearingDay() {
        //For JPA
    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public void setId(HearingSnapshotKey id) {
        this.id = id;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public void setHearing(Hearing hearing) {
        this.hearing = hearing;
    }

    public ZonedDateTime getSittingDay() {
        return sittingDay;
    }

    public void setSittingDay(ZonedDateTime sittingDay) {
        this.sittingDay = sittingDay;
    }

    public Integer getListingSequence() {
        return listingSequence;
    }

    public void setListingSequence(Integer listingSequence) {
        this.listingSequence = listingSequence;
    }

    public Integer getListedDurationMinutes() {
        return listedDurationMinutes;
    }

    public void setListedDurationMinutes(Integer listedDurationMinutes) {
        this.listedDurationMinutes = listedDurationMinutes;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Boolean getIsCancelled() {
        return isCancelled;
    }

    public void setIsCancelled(final Boolean isCancelled) {
        this.isCancelled = isCancelled;
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
        return Objects.equals(this.id, ((HearingDay) o).id);
    }
}