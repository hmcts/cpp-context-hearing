package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.ZonedDateTime;
import java.util.UUID;

@Event("hearing.hearing-initiated")
public class HearingInitiated {

    private UUID hearingId;

    private ZonedDateTime startDateTime;

    private Integer duration;

    private String hearingType;

    public HearingInitiated(UUID hearingId, ZonedDateTime startDateTime, Integer duration,String hearingType) {
        this.hearingId = hearingId;
        this.startDateTime = startDateTime;
        this.duration = duration;
        this.hearingType = hearingType;
    }

    public UUID getHearingId() {
        return hearingId;
    }


    public Integer getDuration() {
        return duration;
    }

    public ZonedDateTime getStartDateTime() {
        return startDateTime;
    }

    public String getHearingType() {
        return hearingType;
    }
}
