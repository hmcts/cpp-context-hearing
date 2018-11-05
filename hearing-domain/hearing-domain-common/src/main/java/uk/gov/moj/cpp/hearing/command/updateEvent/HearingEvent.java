package uk.gov.moj.cpp.hearing.command.updateEvent;

import java.io.Serializable;
import java.util.UUID;

public class HearingEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingEventId;
    private String recordedLabel;

    public HearingEvent() {
        // default constructor for Jackson serialisation
    }


    public HearingEvent(final UUID hearingEventId, final String recordedLabel) {

        this.hearingEventId = hearingEventId;
        this.recordedLabel = recordedLabel;
    }


    public UUID getHearingEventId() {
        return hearingEventId;
    }
    public String getRecordedLabel() {
        return recordedLabel;
    }
}
