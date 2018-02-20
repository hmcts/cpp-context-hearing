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

    public HearingInitiated(final UUID hearingId, final ZonedDateTime startDateTime, final Integer duration,
                            final String hearingType) {
        this.hearingId = hearingId;
        this.startDateTime = startDateTime;
        this.duration = duration;
        this.hearingType = hearingType;
    }

    public HearingInitiated() {
        // default constructor for Jackson serialisation
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
