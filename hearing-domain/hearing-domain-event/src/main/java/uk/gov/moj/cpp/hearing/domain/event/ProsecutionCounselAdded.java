package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.ZonedDateTime;
import java.util.UUID;

@Event("hearing.prosecution-counsel-added")
public class ProsecutionCounselAdded {

    private final UUID personId;
    private final UUID attendeeId;
    private final UUID hearingId;
    private final String status;

    public ProsecutionCounselAdded(final UUID hearingId, final UUID attendeeId, final UUID personId,
                                   final String status) {

        this.hearingId = hearingId;
        this.attendeeId = attendeeId;
        this.personId = personId;
        this.status = status;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getAttendeeId() {
        return attendeeId;
    }

    public UUID getPersonId() {
        return personId;
    }

    public String getStatus() {
        return status;
    }
}
