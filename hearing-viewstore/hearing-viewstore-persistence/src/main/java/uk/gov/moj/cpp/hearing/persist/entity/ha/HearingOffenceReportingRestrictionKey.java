package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class HearingOffenceReportingRestrictionKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "hearing_id", nullable = false)
    private UUID hearingId;

    @Column(name = "offence_id")
    private UUID offenceId;

    public HearingOffenceReportingRestrictionKey() {
    }

    public HearingOffenceReportingRestrictionKey(final UUID id, final UUID hearingId, final UUID offenceId) {
        this.id = id;
        this.hearingId = hearingId;
        this.offenceId = offenceId;
    }

    public UUID getId() {
        return id;
    }

    public HearingOffenceReportingRestrictionKey setId(final UUID id) {
        this.id = id;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public HearingOffenceReportingRestrictionKey setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public HearingOffenceReportingRestrictionKey setOffenceId(final UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final HearingOffenceReportingRestrictionKey that = (HearingOffenceReportingRestrictionKey) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) {
            return false;
        }
        if (getHearingId() != null ? !getHearingId().equals(that.getHearingId()) : that.getHearingId() != null) {
            return false;
        }
        return getOffenceId() != null ? getOffenceId().equals(that.getOffenceId()) : that.getOffenceId() == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.hearingId, this.offenceId);
    }

    @Override
    public String toString() {
        return "HearingSnapshotKey [id=" + id + ", hearingId=" + hearingId + "]";
    }
}