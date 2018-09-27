package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.Plea;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.events.inherited-plea")
public class InheritedPlea implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private Plea plea;

    public InheritedPlea() {

    }

    @JsonCreator
    public InheritedPlea(@JsonProperty("hearingId") final UUID hearingId,
                         @JsonProperty("plea") final Plea plea) {
        this.hearingId = hearingId;
        this.plea = plea;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public Plea getPlea() { return plea; }

    public InheritedPlea setPlea(final Plea plea) {
        this.plea = plea;
        return this;
    }

    public InheritedPlea setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public static InheritedPlea inheritedPlea(){
        return new InheritedPlea();
    }
}
