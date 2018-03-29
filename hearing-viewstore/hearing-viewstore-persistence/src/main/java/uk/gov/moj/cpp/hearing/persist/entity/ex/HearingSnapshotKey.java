package uk.gov.moj.cpp.hearing.persist.entity.ex;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class HearingSnapshotKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "hearing_id", nullable = false)
    private UUID hearingId;

    public HearingSnapshotKey() {

    }

    public HearingSnapshotKey(final UUID id, final UUID hearingId) {
        this.id = id;
        this.hearingId = hearingId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.hearingId);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.id, ((HearingSnapshotKey) o).id)
                && Objects.equals(this.hearingId, ((HearingSnapshotKey) o).hearingId);
    }
}