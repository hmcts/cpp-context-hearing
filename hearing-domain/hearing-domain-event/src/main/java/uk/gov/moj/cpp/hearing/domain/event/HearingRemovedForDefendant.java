package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.hearing-removed-for-defendant")
public class HearingRemovedForDefendant implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID defendantId;

    private UUID hearingId;

    @JsonCreator
    public HearingRemovedForDefendant(@JsonProperty("defendantId") final UUID defendantId,
                                      @JsonProperty("hearingId") final UUID hearingId) {
        this.hearingId = hearingId;
        this.defendantId = defendantId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(final UUID defendantId) {
        this.defendantId = defendantId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }
}
