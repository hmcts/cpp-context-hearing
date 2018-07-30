package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

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

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public LookupWitnessesOnDefendantForHearingCommand setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public LookupWitnessesOnDefendantForHearingCommand setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public static LookupWitnessesOnDefendantForHearingCommand lookupWitnessesOnDefendantForHearingCommand(){
        return new LookupWitnessesOnDefendantForHearingCommand();
    }
}
