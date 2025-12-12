package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.hearing-deleted-bdf")
@SuppressWarnings({"squid:S2384", "pmd:BeanMembersShouldSerialize"})
public class HearingDeletedBdf implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    @JsonCreator
    public HearingDeletedBdf(@JsonProperty("hearingId") final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
