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
@Table(name = "ha_hearing_date")
public class HearingDate {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Hearing hearing;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "date_time")
    private ZonedDateTime dateTime;


    public HearingDate() {
    }

    public HearingDate(Builder builder) {
        this.id = builder.id;
        this.hearing = builder.hearing;
        this.dateTime = builder.dateTime;
        this.date = builder.date;

    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public LocalDate getDate() {
        return date;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public static class Builder {
        private HearingSnapshotKey id;
        private Hearing hearing;
        private LocalDate date;
        private ZonedDateTime dateTime;


        protected Builder() {}
        public Builder withId(HearingSnapshotKey id) {
            this.id = id;
            return this;
        }


        public Builder withHearing(Hearing hearing) {
            this.hearing = hearing;
            return this;
        }

        public Builder withDate(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder withDateTime(ZonedDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }


        public HearingDate build() {
            return new HearingDate(this);
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
        return Objects.equals(this.id, ((HearingDate)o).id);
    }
}