package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.hearing-event-deleted")
public class HearingEventDeleted implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingEventId;

    @JsonCreator
    public HearingEventDeleted(@JsonProperty("hearingEventId") final UUID hearingEventId) {
        this.hearingEventId = hearingEventId;
    }

    public HearingEventDeleted() {
        // default constructor for Jackson serialisation
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

}
