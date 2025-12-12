package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import java.io.Serializable;
import java.util.UUID;

@Event("hearing.events.earliest-next-hearing-date-cleared")
public class EarliestNextHearingDateCleared implements Serializable {

    private static final long serialVersionUID = 1406265925731539375L;

    private final UUID hearingId;

    @JsonCreator
    public EarliestNextHearingDateCleared(@JsonProperty("hearingId") final UUID hearingId) {

        this.hearingId = hearingId;

    }

    public UUID getHearingId() {
        return this.hearingId;
    }

}
