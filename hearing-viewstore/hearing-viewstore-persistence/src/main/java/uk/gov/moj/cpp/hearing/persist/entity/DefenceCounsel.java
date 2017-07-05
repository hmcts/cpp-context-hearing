package uk.gov.moj.cpp.hearing.persist.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "hearing_defence_counsel")
public class DefenceCounsel {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID attendeeId;

    @Column(name = "hearing_id")
    private UUID hearingId;

    @Column(name = "person_id")
    private UUID personId;

    @Column(name = "status")
    private String status;

    public DefenceCounsel() {
        // for JPA
    }
    public DefenceCounsel(final UUID attendeeId, final UUID hearingId, final UUID personId, final String status) {
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

}
