package uk.gov.moj.cpp.hearing.persist.entity;

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
    private UUID id;

    @Column(name = "hearingid")
    private UUID hearingId;

    @Column(name = "personid")
    private UUID personId;

    @Column(name = "status")
    private String status;

    public ProsecutionCounsel() {
        // for JPA
    }

    public ProsecutionCounsel(final UUID id, final UUID hearingId, final UUID personId, final String status) {
        this.id = id;
        this.hearingId = hearingId;
        this.personId = personId;
        this.status = status;
    }

    public UUID getId() {
        return id;
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
