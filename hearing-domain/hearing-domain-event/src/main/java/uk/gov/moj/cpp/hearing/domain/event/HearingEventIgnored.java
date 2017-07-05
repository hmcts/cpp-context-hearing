package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.ZonedDateTime;
import java.util.UUID;

@Event("hearing.hearing-event-ignored")
public class HearingEventIgnored {

    private final UUID hearingEventId;
    private final UUID hearingId;
    private final String recordedLabel;
    private final ZonedDateTime eventTime;
    private final String reason;

    public HearingEventIgnored(final UUID hearingEventId, final UUID hearingId, final String recordedLabel,
                               final ZonedDateTime eventTime, final String reason) {
        this.hearingEventId = hearingEventId;
        this.hearingId = hearingId;
        this.recordedLabel = recordedLabel;
        this.eventTime = eventTime;
        this.reason = reason;
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public String getRecordedLabel() {
        return recordedLabel;
    }

    public ZonedDateTime getEventTime() {
        return eventTime;
    }

    public String getReason() {
        return reason;
    }

}
