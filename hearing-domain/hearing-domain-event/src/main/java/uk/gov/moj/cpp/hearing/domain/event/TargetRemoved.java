package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.target-removed")
public class TargetRemoved implements Serializable {

    private static final long serialVersionUID = 1493964448765706069L;

    private UUID hearingId;

    private UUID targetId;

    public TargetRemoved() {
    }

    @JsonCreator
    public TargetRemoved(
            @JsonProperty("hearingId") UUID hearingId,
            @JsonProperty("targetId") UUID targetId) {
        super();
        this.hearingId = hearingId;
        this.targetId = targetId;
    }

    public static TargetRemoved targetRemoved() {
        return new TargetRemoved();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public TargetRemoved setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public TargetRemoved setTargetId(UUID targetID) {
        this.targetId = targetID;
        return this;
    }
}
