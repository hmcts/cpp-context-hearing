package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ha_draft_result")
public class DraftResult {


    @Id
    @Column(name = "draft_result_id", nullable = false)
    private String draftResultId;

    @Column(name = "hearing_id", nullable = false)
    private UUID hearingId;

    @Column(name = "hearing_day")
    private String hearingDay;

    @Column(name = "draft_result", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode draftResultPayload;

    @Column(name = "user_id")
    private UUID amendedByUserId;

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public JsonNode getDraftResultPayload() {
        return draftResultPayload;
    }

    public void setDraftResultPayload(final JsonNode draftResultPayload) {
        this.draftResultPayload = draftResultPayload;
    }

    public UUID getAmendedByUserId() {
        return amendedByUserId;
    }

    public void setAmendedByUserId(final UUID amendedByUserId) {
        this.amendedByUserId = amendedByUserId;
    }

    public void setHearingDay(String hearingDay) {
        this.hearingDay = hearingDay;
    }

    public String getHearingDay() {
        return hearingDay;
    }

    public String getDraftResultId() {
        return draftResultId;
    }

    public void setDraftResultId(final String draftResultId) {
        this.draftResultId = draftResultId;
    }
}
