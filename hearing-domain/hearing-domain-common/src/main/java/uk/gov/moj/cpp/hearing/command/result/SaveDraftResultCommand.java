package uk.gov.moj.cpp.hearing.command.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.json.schemas.core.Target;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
public final class SaveDraftResultCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private Target target;
    private UUID hearingId;

    @JsonCreator
    public SaveDraftResultCommand(@JsonProperty("target") final Target target,
                                  @JsonProperty("hearingId") final UUID hearingId) {
        this.target = target;
        this.hearingId = hearingId;
    }

    public Target getTarget() {
        return target;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }
}