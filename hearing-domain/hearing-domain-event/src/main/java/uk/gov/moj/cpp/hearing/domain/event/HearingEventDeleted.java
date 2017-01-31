package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.hearing-event-deleted")
public class HearingEventDeleted {

    private final UUID hearingEventId;

    public HearingEventDeleted(final UUID hearingEventId) {
        this.hearingEventId = hearingEventId;
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

}
