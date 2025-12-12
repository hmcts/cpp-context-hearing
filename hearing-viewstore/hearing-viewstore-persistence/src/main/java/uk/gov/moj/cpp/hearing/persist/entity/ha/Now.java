package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ha_now")
public class Now {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;


    @Column(name = "hearing_id", nullable = false)
    private UUID hearingId;

    @Column(name = "payload")
    private String payload;

    public Now() {

    }

    public Now(final UUID id, final UUID hearingId, final String payload) {
        this.id = id;
        this.hearingId = hearingId;
        this.payload = payload;
    }
    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(final String payload) {
        this.payload = payload;
    }
}
