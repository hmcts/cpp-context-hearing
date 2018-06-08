package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.*;

@Entity
@Table(name = "ha_attendee_hearing_date")
public class AttendeeHearingDate {

    @EmbeddedId
    private HearingSnapshotKey id;

    @Column(name = "attendee_id")
    private UUID attendeeId;

    @Column(name = "hearing_date_id")
    private UUID hearingDateId;

    public AttendeeHearingDate() {
    }

    public AttendeeHearingDate(final Builder builder) {
        this.id = builder.id;
        this.attendeeId = builder.attendeeId;
        this.hearingDateId = builder.hearingDateId;
    }

    public HearingSnapshotKey getId() {
        return this.id;
    }

    public UUID getAttendeeId() {
        return attendeeId;
    }

    public UUID getHearingDateId() {
        return this.hearingDateId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.attendeeId, this.hearingDateId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AttendeeHearingDate other = (AttendeeHearingDate) obj;
        return Objects.equals(this.id, other.id) && Objects.equals(this.attendeeId, other.attendeeId)
                && Objects.equals(this.getHearingDateId(), other.hearingDateId);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private HearingSnapshotKey id;
        private UUID attendeeId;
        private UUID hearingDateId;

        public Builder withId(final HearingSnapshotKey id) {
            this.id = id;
            return this;
        }

        public Builder withAttendeeId(final UUID attendeeId) {
            this.attendeeId = attendeeId;
            return this;
        }

        public Builder withHearingDateId(UUID hearingDateId) {
            this.hearingDateId = hearingDateId;
            return this;
        }

        public AttendeeHearingDate build() {
            return new AttendeeHearingDate(this);
        }
    }
}