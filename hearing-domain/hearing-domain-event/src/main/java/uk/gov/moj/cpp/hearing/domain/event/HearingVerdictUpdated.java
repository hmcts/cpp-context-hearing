package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.hearing-verdict-updated")
public class HearingVerdictUpdated implements Serializable {

    private static final long serialVersionUID = 1L;
    private UUID hearingId;

    @JsonCreator
    public HearingVerdictUpdated(
            @JsonProperty("hearingId") final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public HearingVerdictUpdated() {
        // default constructor for Jackson serialisation
    }

    public UUID getHearingId() {
        return hearingId;
    }

}
