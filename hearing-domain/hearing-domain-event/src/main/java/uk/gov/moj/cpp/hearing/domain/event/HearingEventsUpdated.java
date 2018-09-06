package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Event("hearing.hearing-events-updated")
public class HearingEventsUpdated implements Serializable {

    private static final long serialVersionUID = 1L;
    private UUID hearingId;
    private List<HearingEvent> hearingEvents = new ArrayList<>();

    public HearingEventsUpdated() {
        // default constructor for Jackson serialisation
    }

    @JsonCreator
    public HearingEventsUpdated(@JsonProperty("hearingId") final UUID hearingId,
                                @JsonProperty("hearingEvents") final List<HearingEvent> hearingEvents) {
        this.hearingId = hearingId;
        this.hearingEvents = hearingEvents;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<HearingEvent> getHearingEvents() {
        return hearingEvents;
    }
}