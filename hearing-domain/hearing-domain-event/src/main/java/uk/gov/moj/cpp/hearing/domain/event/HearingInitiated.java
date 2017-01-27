package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.ZonedDateTime;
import java.util.UUID;

@Event("hearing.hearing-initiated")
public class HearingInitiated {

    private final UUID hearingId;
    private final ZonedDateTime startDateTime;
    private final Integer duration;
    private final String hearingType;

    public HearingInitiated(final UUID hearingId, final ZonedDateTime startDateTime, final Integer duration,
                            final String hearingType) {
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
