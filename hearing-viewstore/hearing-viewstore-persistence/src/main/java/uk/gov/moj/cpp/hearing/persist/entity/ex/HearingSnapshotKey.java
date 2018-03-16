package uk.gov.moj.cpp.hearing.persist.entity.ex;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class HearingSnapshotKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name ="id", nullable=false)
    private UUID id;

    @Column(name="hearing_id", nullable=false)
    private UUID hearingId;

    public HearingSnapshotKey() {

    }
    public HearingSnapshotKey(final UUID id, final UUID hearingId) {
        this.id=id;
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hearingId == null) ? 0 : hearingId.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HearingSnapshotKey other = (HearingSnapshotKey) obj;
        if (hearingId == null) {
            if (other.hearingId != null)
                return false;
        } else if (!hearingId.equals(other.hearingId))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
