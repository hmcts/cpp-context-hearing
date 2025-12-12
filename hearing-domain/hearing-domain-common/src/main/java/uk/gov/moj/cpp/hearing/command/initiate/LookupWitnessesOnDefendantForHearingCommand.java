package uk.gov.moj.cpp.hearing.command.initiate;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LookupWitnessesOnDefendantForHearingCommand {

    private UUID hearingId;

    private UUID defendantId;

    public LookupWitnessesOnDefendantForHearingCommand() {
    }

    @JsonCreator
    public LookupWitnessesOnDefendantForHearingCommand(
            @JsonProperty("hearingId") UUID hearingId,
            @JsonProperty("defendantId") UUID defendantId) {
        this.hearingId = hearingId;
        this.defendantId = defendantId;
    }

    public static LookupWitnessesOnDefendantForHearingCommand lookupWitnessesOnDefendantForHearingCommand() {
        return new LookupWitnessesOnDefendantForHearingCommand();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public LookupWitnessesOnDefendantForHearingCommand setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public LookupWitnessesOnDefendantForHearingCommand setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }
}
