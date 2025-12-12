package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class HearingYouthCourDefendantsKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "hearing_id", nullable = false)
    private UUID hearingId;

    @Column(name = "defendant_id", nullable = false)
    private UUID defendantId;

    public HearingYouthCourDefendantsKey() {
        //For JPA
    }

    public HearingYouthCourDefendantsKey(final UUID defendantId, final UUID hearingId) {
        this.defendantId = defendantId;
        this.hearingId = hearingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.defendantId, this.hearingId);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.defendantId, ((HearingYouthCourDefendantsKey) o).defendantId)
                && Objects.equals(this.hearingId, ((HearingYouthCourDefendantsKey) o).hearingId);
    }

    @Override
    public String toString() {
        return "HearingYouthCourDefendantsKey{" +
                "hearingId=" + hearingId +
                ", defendantId=" + defendantId +
                '}';
    }
}
