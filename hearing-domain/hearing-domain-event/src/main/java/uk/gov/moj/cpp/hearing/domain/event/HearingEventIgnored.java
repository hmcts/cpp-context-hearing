package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonCreator
    public HearingEventIgnored(@JsonProperty("hearingEventId") final UUID hearingEventId,
                               @JsonProperty("hearingId") final UUID hearingId,
                               @JsonProperty("hearingEventDefinitionId") final UUID hearingEventDefinitionId,
                               @JsonProperty("recordedLabel") final String recordedLabel,
                               @JsonProperty("eventTime") final ZonedDateTime eventTime,
                               @JsonProperty("reason") final String reason,
                               @JsonProperty("alterable") final boolean alterable) {
        this.hearingEventId = hearingEventId;
        this.hearingId = hearingId;
        this.hearingEventDefinitionId = hearingEventDefinitionId;
        this.recordedLabel = recordedLabel;
        this.eventTime = eventTime;
        this.reason = reason;
        this.alterable = alterable;
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
