package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("pmd:BeanMembersShouldSerialize")
@Event("hearing.hearing-event-deleted")
public class HearingEventDeleted implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingEventId;
    private UUID userId;

    @JsonCreator
    public HearingEventDeleted(@JsonProperty("hearingEventId") final UUID hearingEventId,
                               @JsonProperty("userId") final UUID userId) {
        this.hearingEventId = hearingEventId;
        this.userId = userId;
    }

    public HearingEventDeleted() {
        // default constructor for Jackson serialisation
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    public UUID getUserId() {
        return userId;
    }
}
