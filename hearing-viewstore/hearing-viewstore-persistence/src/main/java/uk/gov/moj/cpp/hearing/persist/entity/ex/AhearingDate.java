package uk.gov.moj.cpp.hearing.persist.entity.ex;

import java.time.ZonedDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;



@Entity
@Table(name = "a_hearing_date")
public class AhearingDate {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Ahearing hearing;

    @Column(name = "date")
    private ZonedDateTime date;


    public AhearingDate() {
    }

    public AhearingDate(Builder builder) {
        this.id = builder.id;
        this.hearing = builder.hearing;
        this.date = builder.date;

    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public Ahearing getHearing() {
        return hearing;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public static class Builder {
        private HearingSnapshotKey id;
        private Ahearing hearing;
        private ZonedDateTime date;


        protected Builder() {}
        public Builder withId(HearingSnapshotKey id) {
            this.id = id;
            return this;
        }


        public Builder withHearing(Ahearing hearing) {
            this.hearing = hearing;
            return this;
        }

        public Builder withDate(ZonedDateTime date) {
            this.date = date;
            return this;
        }


        public AhearingDate build() {
            return new AhearingDate(this);
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
        return Objects.equals(this.id, ((AhearingDate)o).id);
    }
}