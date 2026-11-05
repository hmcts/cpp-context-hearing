package uk.gov.moj.cpp.hearing.persist.entity.ha;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "ha_case_marker")
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
    @JdbcTypeCode(SqlTypes.JSON)
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