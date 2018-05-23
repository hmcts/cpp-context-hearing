package uk.gov.moj.cpp.hearing.command.updateEvent;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

public class HearingEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingEventId;
    private UUID hearingEventDefinitionId;
    private ZonedDateTime lastModifiedTime;
    private String recordedLabel;
    private ZonedDateTime eventTime;
    private UUID witnessId;

    public HearingEvent() {
        // default constructor for Jackson serialisation
    }


    public HearingEvent(final UUID hearingEventId, final UUID hearingEventDefinitionId,
                    final ZonedDateTime lastModifiedTime,
                    final String recordedLabel,
                    final ZonedDateTime eventTime,
                    final UUID witnessId) {

        this.hearingEventDefinitionId = hearingEventDefinitionId;
        this.hearingEventId = hearingEventId;
        this.recordedLabel = recordedLabel;
        this.eventTime = eventTime;
        this.lastModifiedTime = lastModifiedTime;
        this.witnessId = witnessId;
    }

    public UUID getHearingEventDefinitionId() {
        return hearingEventDefinitionId;
    }

    public UUID getHearingEventId() {
        return hearingEventId;
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

    public UUID getWitnessId() {
        return witnessId;
    }

}
