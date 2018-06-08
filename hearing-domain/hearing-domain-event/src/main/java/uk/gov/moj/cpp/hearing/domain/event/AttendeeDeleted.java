package uk.gov.moj.cpp.hearing.domain.event;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.justice.domain.annotation.Event;

@Event("hearing.events.attendee-deleted")
public final class AttendeeDeleted {

    private final UUID hearingId;
    private final UUID attendeeId;
    private final LocalDate hearingDate;

    @JsonCreator
    public AttendeeDeleted(@JsonProperty(value = "hearingId", required = true) final UUID hearingId, 
            @JsonProperty(value = "attendeeId", required = true) final UUID attendeeId,
            @JsonProperty(value = "hearingDate", required = true) final LocalDate hearingDate) {
        this.hearingId = hearingId;
        this.attendeeId = attendeeId;
        this.hearingDate = hearingDate;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getAttendeeId() {
        return attendeeId;
    }

    public LocalDate getHearingDate() {
        return hearingDate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.hearingId, this.attendeeId, this.hearingDate);
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
        final AttendeeDeleted other = (AttendeeDeleted) obj;
        return Objects.equals(this.hearingId, other.hearingId) && Objects.equals(this.attendeeId, other.attendeeId)
                && Objects.equals(this.hearingDate, other.hearingDate);
    }

    @Override
    public String toString() {
        return "AttendeeDeleted [hearingId=" + hearingId + ", attendeeId=" + attendeeId + ", hearingDate=" + hearingDate + "]";
    }
}