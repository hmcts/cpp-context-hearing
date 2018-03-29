package uk.gov.moj.cpp.hearing.persist.entity.ex;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DefendantCaseKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "hearing_id", nullable = false)
    private UUID hearingId;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "defendant_id", nullable = false)
    private UUID defendantId;

    public DefendantCaseKey() {

    }

    public DefendantCaseKey(UUID hearingId, UUID caseId, UUID defendantId) {
        this.hearingId = hearingId;
        this.caseId = caseId;
        this.defendantId = defendantId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public void setCaseId(UUID caseId) {
        this.caseId = caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.caseId, this.defendantId, this.hearingId);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.caseId, ((DefendantCaseKey) o).caseId)
                && Objects.equals(this.defendantId, ((DefendantCaseKey) o).defendantId)
                && Objects.equals(this.hearingId, ((DefendantCaseKey) o).hearingId);
    }
}