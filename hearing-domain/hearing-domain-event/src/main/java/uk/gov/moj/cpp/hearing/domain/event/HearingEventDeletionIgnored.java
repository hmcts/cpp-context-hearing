package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.hearing-event-deletion-ignored")
public class HearingEventDeletionIgnored {

    private final UUID hearingEventId;
    private final String reason;

    public HearingEventDeletionIgnored(final UUID hearingEventId, final String reason) {
        this.hearingEventId = hearingEventId;
        this.reason = reason;
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    public String getReason() {
        return reason;
    }

}
