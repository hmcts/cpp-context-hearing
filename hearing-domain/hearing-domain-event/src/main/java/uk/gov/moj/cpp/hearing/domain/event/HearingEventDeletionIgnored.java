package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.hearing-event-deletion-ignored")
public class HearingEventDeletionIgnored {

    private UUID hearingEventId;
    private String reason;

    public HearingEventDeletionIgnored(final UUID hearingEventId, final String reason) {
        this.hearingEventId = hearingEventId;
        this.reason = reason;
    }

    public HearingEventDeletionIgnored() {
        // default constructor for Jackson serialisation
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    public String getReason() {
        return reason;
    }

}
