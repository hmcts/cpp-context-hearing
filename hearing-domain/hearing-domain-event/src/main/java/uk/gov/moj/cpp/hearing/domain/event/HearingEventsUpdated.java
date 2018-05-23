package uk.gov.moj.cpp.hearing.domain.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent;

@Event("hearing.hearing-events-updated")
public class HearingEventsUpdated implements Serializable {

    private static final long serialVersionUID = 1L;
    private UUID hearingId;
    private List<HearingEvent> hearingEvents;

    public HearingEventsUpdated() {
        // default constructor for Jackson serialisation
    }


    public HearingEventsUpdated(final UUID hearingId, final List<HearingEvent> hearingEvents) {
        this.hearingId = hearingId;
        this.hearingEvents = hearingEvents;
    }


    public UUID getHearingId() {
        return hearingId;
    }


    public List<HearingEvent> getHearingEvents() {
        if (hearingEvents == null) {
            hearingEvents = new ArrayList<>();
        }
        return hearingEvents;
    }

}
