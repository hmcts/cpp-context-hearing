package uk.gov.moj.cpp.hearing.domain.event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import uk.gov.justice.domain.annotation.Event;

@Event("hearing.hearing-event-ignored")
public class HearingEventIgnored implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingEventId;
    private UUID hearingId;
    private UUID hearingEventDefinitionId;
    private String recordedLabel;
    private ZonedDateTime eventTime;
    private String reason;
    private boolean alterable;

    public HearingEventIgnored(final UUID hearingEventId, final UUID hearingId, final UUID hearingEventDefinitionId, final String recordedLabel,
                               final ZonedDateTime eventTime, final String reason, final boolean alterable) {
        this.hearingEventId = hearingEventId;
        this.hearingId = hearingId;
        this.hearingEventDefinitionId = hearingEventDefinitionId;
        this.recordedLabel = recordedLabel;
        this.eventTime = eventTime;
        this.reason = reason;
        this.alterable = alterable;
    }

    public HearingEventIgnored() {
        // default constructor for Jackson serialisation
    }

    public HearingEventIgnored(final UUID hearingId, final String reason) {
        this.hearingId = hearingId;
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

    public boolean isAlterable() {
        return alterable;
    }

    public UUID getHearingEventDefinitionId() {
        return hearingEventDefinitionId;
    }
}
