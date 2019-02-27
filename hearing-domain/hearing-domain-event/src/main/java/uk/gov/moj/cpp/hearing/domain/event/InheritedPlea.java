package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.Plea;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    public static InheritedPlea inheritedPlea() {
        return new InheritedPlea();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public InheritedPlea setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public Plea getPlea() {
        return plea;
    }

    public InheritedPlea setPlea(final Plea plea) {
        this.plea = plea;
        return this;
    }
}
