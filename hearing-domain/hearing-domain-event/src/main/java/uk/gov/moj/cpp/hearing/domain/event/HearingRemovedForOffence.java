package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.hearing-removed-for-offence")
public class HearingRemovedForOffence implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID offenceId;

    private UUID hearingId;

    @JsonCreator
    public HearingRemovedForOffence(@JsonProperty("offenceId") final UUID defendantId,
                                    @JsonProperty("hearingId") final UUID hearingId) {
        this.hearingId = hearingId;
        this.offenceId = defendantId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public void setOffenceId(final UUID offenceId) {
        this.offenceId = offenceId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }
}
