package uk.gov.moj.cpp.hearing.command.hearing.details;

import uk.gov.moj.cpp.hearing.domain.HearingState;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HearingAmendCommand {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;
    private final HearingState newHearingState;

    @JsonCreator
    public HearingAmendCommand(@JsonProperty("hearingId") final UUID hearingId,
                @JsonProperty("newHearingState") final HearingState newHearingState) {
        this.hearingId = hearingId;
        this.newHearingState = newHearingState;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public HearingState getNewHearingState() {
        return newHearingState;
    }
}

