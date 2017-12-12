package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.prosecution-counsel-added")
public class ProsecutionCounselAdded {
    private UUID personId;
    private UUID attendeeId;
    private UUID hearingId;
    private String status;

    public ProsecutionCounselAdded(final UUID hearingId, final UUID attendeeId, final UUID personId,
                                   final String status) {
        this.hearingId = hearingId;
        this.attendeeId = attendeeId;
        this.personId = personId;
        this.status = status;
    }

    public ProsecutionCounselAdded() {
        // default constructor for Jackson serialisation
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
