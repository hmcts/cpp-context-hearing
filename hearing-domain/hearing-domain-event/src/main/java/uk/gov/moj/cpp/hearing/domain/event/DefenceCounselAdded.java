package uk.gov.moj.cpp.hearing.domain.event;

import static java.util.Collections.unmodifiableList;

import uk.gov.justice.domain.annotation.Event;

import java.util.List;
import java.util.UUID;

@Event("hearing.defence-counsel-added")
public class DefenceCounselAdded {

    private UUID personId;
    private UUID attendeeId;
    private List<UUID> defendantIds;
    private UUID hearingId;
    private String status;

    public DefenceCounselAdded(final UUID hearingId, final UUID attendeeId, final UUID personId,
                               final List<UUID> defendantIds, final String status) {

        this.hearingId = hearingId;
        this.attendeeId = attendeeId;
        this.personId = personId;
        this.defendantIds = unmodifiableList(defendantIds);
        this.status = status;
    }

    public DefenceCounselAdded() {
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

    public List<UUID> getDefendantIds() {
        return unmodifiableList(defendantIds);
    }

    public String getStatus() {
        return status;
    }
}
