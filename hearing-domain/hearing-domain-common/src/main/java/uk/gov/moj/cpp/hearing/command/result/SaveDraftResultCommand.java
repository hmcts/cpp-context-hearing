package uk.gov.moj.cpp.hearing.command.result;

import uk.gov.justice.core.courts.Target;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
public final class SaveDraftResultCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private Target target;
    private UUID hearingId;

    public SaveDraftResultCommand() {
    }

    @JsonCreator
    public SaveDraftResultCommand(@JsonProperty("target") final Target target,
                                  @JsonProperty("hearingId") final UUID hearingId) {
        this.target = target;
        this.hearingId = hearingId;
    }

    public static SaveDraftResultCommand saveDraftResultCommand() {
        return new SaveDraftResultCommand();
    }

    public Target getTarget() {
        return target;
    }

    public SaveDraftResultCommand setTarget(Target target) {
        this.target = target;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public SaveDraftResultCommand setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }
}