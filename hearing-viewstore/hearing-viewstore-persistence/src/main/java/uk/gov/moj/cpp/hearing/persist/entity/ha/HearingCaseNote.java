package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@Table(name = "ha_hearing_case_note")
@TypeDef(
        name = "jsonb-node",
        typeClass = JsonNodeBinaryType.class
)
public class HearingCaseNote {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Hearing hearing;

    @Column(name = "payload", columnDefinition = "jsonb")
    @Type(type = "jsonb-node")
    private JsonNode payload;


    public HearingCaseNote() {
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
}