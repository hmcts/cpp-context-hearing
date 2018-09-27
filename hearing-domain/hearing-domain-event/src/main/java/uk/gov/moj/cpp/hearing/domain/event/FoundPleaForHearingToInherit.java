package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.Plea;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.events.found-plea-for-hearing-to-inherit")
public class FoundPleaForHearingToInherit implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private Plea plea;

    @JsonCreator
    public FoundPleaForHearingToInherit(@JsonProperty("hearingId") UUID hearingId,
                                        @JsonProperty("plea") Plea plea) {
        this.hearingId = hearingId;
        this.plea = plea;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public Plea getPlea() {
        return this.plea;
    }
}
