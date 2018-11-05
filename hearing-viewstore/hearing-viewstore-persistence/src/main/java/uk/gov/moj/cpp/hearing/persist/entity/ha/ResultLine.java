package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "result_line")
public class ResultLine {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Hearing hearing;

    @Column(name = "last_shared_date_time")
    private ZonedDateTime lastSharedDateTime;

    public ResultLine() {
        //For JPA
    }

    public ResultLine(Builder builder) {
        this.id = builder.id;
        this.hearing = builder.hearing;
        this.lastSharedDateTime = builder.lastSharedDateTime;

    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public ZonedDateTime getLastSharedDateTime() {
        return lastSharedDateTime;
    }

    public void setLastSharedDateTime(final ZonedDateTime lastSharedDateTime) {
        this.lastSharedDateTime = lastSharedDateTime;
    }

    public static class Builder {
        private HearingSnapshotKey id;
        private Hearing hearing;
        private ZonedDateTime lastSharedDateTime;

        protected Builder() {
        }

        public Builder withId(HearingSnapshotKey id) {
            this.id = id;
            return this;
        }

        public Builder withHearing(Hearing hearing) {
            this.hearing = hearing;
            return this;
        }

        public Builder withLastSharedDateTime(ZonedDateTime lastSharedDateTime) {
            this.lastSharedDateTime = lastSharedDateTime;
            return this;
        }

        public ResultLine build() {
            return new ResultLine(this);
        }

    }

    public static Builder builder() {
        return new Builder();
    }
}