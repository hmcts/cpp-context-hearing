package uk.gov.moj.cpp.hearing.persist.entity;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "hearing_prosecution_counsel")
public class ProsecutionCounsel {

    @Id
    @Column(name = "id", nullable = false)
    private UUID attendeeId;

    @Column(name = "hearingid")
    private UUID hearingId;

    @Column(name = "personid")
    private UUID personId;

    @Column(name = "status")
    private String status;

    public ProsecutionCounsel() {
        // for JPA
    }

    public ProsecutionCounsel(final UUID attendeeId, final UUID hearingId, final UUID personId, final String status) {
        this.attendeeId = attendeeId;
        this.hearingId = hearingId;
        this.personId = personId;
        this.status = status;
    }

    public UUID getAttendeeId() {
        return attendeeId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getPersonId() {
        return personId;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProsecutionCounsel that = (ProsecutionCounsel) o;
        return Objects.equals(getAttendeeId(), that.getAttendeeId()) &&
                Objects.equals(getHearingId(), that.getHearingId()) &&
                Objects.equals(getPersonId(), that.getPersonId()) &&
                Objects.equals(getStatus(), that.getStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAttendeeId(), getHearingId(), getPersonId(), getStatus());
    }

    @Override
    public String toString() {
        return "ProsecutionCounsel{" +
                "attendeeId=" + attendeeId +
                ", hearingId=" + hearingId +
                ", personId=" + personId +
                ", status='" + status + '\'' +
                '}';
    }
}
