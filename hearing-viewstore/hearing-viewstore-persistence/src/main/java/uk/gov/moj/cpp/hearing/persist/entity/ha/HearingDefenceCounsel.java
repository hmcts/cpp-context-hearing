package uk.gov.moj.cpp.hearing.persist.entity.ha;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ha_hearing_defence_counsel")
public class HearingDefenceCounsel {

    @Id
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Hearing hearing;

    @Column(name = "payload", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode payload;

    @Column(name = "deleted")
    private boolean deleted;

    public HearingDefenceCounsel() {
        //For JPA
    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public void setId(final HearingSnapshotKey id) {
        this.id = id;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public void setHearing(final Hearing hearing) {
        this.hearing = hearing;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(final JsonNode payload) {
        this.payload = payload;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }
}