package uk.gov.moj.cpp.hearing.nows.events;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.justice.domain.annotation.Event;

@Event("hearing.events.nows-material-status-updated")
public final class NowsMaterialStatusUpdated {

    private final UUID hearingId;
    private final UUID materialId;
    private final String status;

    @JsonCreator
    public NowsMaterialStatusUpdated(@JsonProperty(value = "hearingId", required = true) final UUID hearingId, 
            @JsonProperty(value = "materialId", required = true) final UUID materialId,
            @JsonProperty(value = "status", required = true) final String status) {
        this.hearingId = hearingId;
        this.materialId = materialId;
        this.status = status;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getMaterialId() {
        return materialId;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.hearingId,this.materialId, this.status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NowsMaterialStatusUpdated other = (NowsMaterialStatusUpdated) obj;
        return Objects.equals(this.hearingId, other.hearingId) && Objects.equals(this.materialId, other.materialId)
                && Objects.equals(this.status, other.status);
    }

    @Override
    public String toString() {
        return "NowsMaterialStatusUpdated [hearingId=" + hearingId + ", materialId=" + materialId + ", status=" + status + "]";
    }
}