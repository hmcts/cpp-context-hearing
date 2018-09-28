package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.json.schemas.core.Plea;

import java.io.Serializable;
import java.util.UUID;

public class UpdateHearingWithInheritedPleaCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private Plea plea;

    public UpdateHearingWithInheritedPleaCommand() {
    }

    @JsonCreator
    public UpdateHearingWithInheritedPleaCommand(@JsonProperty("hearingId") final UUID hearingId,
                                                 @JsonProperty("plea") final Plea plea) {
        this.hearingId = hearingId;
        this.plea = plea;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public Plea getPlea() {
        return plea;
    }

    public UpdateHearingWithInheritedPleaCommand setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public UpdateHearingWithInheritedPleaCommand setPlea(final Plea plea) {
        this.plea = plea;
        return this;
    }

    public static UpdateHearingWithInheritedPleaCommand updateHearingWithInheritedPleaCommand(){
        return new UpdateHearingWithInheritedPleaCommand();
    }
}