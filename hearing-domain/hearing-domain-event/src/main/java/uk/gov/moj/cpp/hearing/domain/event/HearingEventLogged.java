package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.ZonedDateTime;
import java.util.UUID;

@Event("hearing.hearing-event-logged")
public class HearingEventLogged {

    private UUID hearingEventId;
    private UUID lastHearingEventId;
    private UUID hearingId;
    private UUID hearingEventDefinitionId;
    private String recordedLabel;
    private ZonedDateTime eventTime;
    private ZonedDateTime lastModifiedTime;
    private boolean alterable;

    public HearingEventLogged(final UUID hearingEventId, final UUID lastHearingEventId, final UUID hearingId, final UUID hearingEventDefinitionId, final String recordedLabel,
                              final ZonedDateTime eventTime, final ZonedDateTime lastModifiedTime, final boolean alterable) {

        this.hearingEventId = hearingEventId;
        this.lastHearingEventId = lastHearingEventId;
        this.hearingId = hearingId;
        this.hearingEventDefinitionId = hearingEventDefinitionId;
        this.recordedLabel = recordedLabel;
        this.eventTime = eventTime;
        this.lastModifiedTime = lastModifiedTime;
        this.alterable = alterable;
    }

    public HearingEventLogged() {
        // default constructor for Jackson serialisation
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    public UUID getLastHearingEventId() {
        return lastHearingEventId;
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

    public ZonedDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public boolean isAlterable() {
        return alterable;
    }

    public UUID getHearingEventDefinitionId() {
        return hearingEventDefinitionId;
    }
}
