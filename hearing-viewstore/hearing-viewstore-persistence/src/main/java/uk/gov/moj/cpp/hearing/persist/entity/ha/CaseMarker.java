package uk.gov.moj.cpp.hearing.persist.entity.ha;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "ha_case_marker")
@TypeDef(
        name = "jsonb-node",
        typeClass = JsonNodeBinaryType.class
)
public class CaseMarker {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "prosecution_case_id", insertable = false, updatable = false, referencedColumnName = "id"),
            @JoinColumn(name = "hearing_id", insertable = false, updatable = false, referencedColumnName = "hearing_id")})
    private ProsecutionCase prosecutionCase;

    @Column(name = "prosecution_case_id")
    private UUID prosecutionCaseId;

    @Column(name = "payload", columnDefinition = "jsonb")
    @Type(type = "jsonb-node")
    private JsonNode payload;


    public CaseMarker() {
        //For JPA
    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public void setId(final HearingSnapshotKey id) {
        this.id = id;
    }

    public ProsecutionCase getProsecutionCase() {
        return prosecutionCase;
    }

    public void setProsecutionCase(ProsecutionCase prosecutionCase) {
        this.prosecutionCase = prosecutionCase;
    }

    public UUID getProsecutionCaseId() {
        return prosecutionCaseId;
    }

    public void setProsecutionCaseId(UUID prosecutionCaseId) {
        this.prosecutionCaseId = prosecutionCaseId;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(final JsonNode payload) {
        this.payload = payload;
    }
}