package uk.gov.moj.cpp.hearing.command.witness;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public class AddDefenceWitnessCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID witnessId;
    private UUID defendantId;
    private UUID hearingId;

    @JsonCreator
    public AddDefenceWitnessCommand(@JsonProperty("witnessId") UUID witnessId,
                                    @JsonProperty("defendantId") UUID defendantId,
                                    @JsonProperty("hearingId") UUID hearingId) {
        this.witnessId = witnessId;
        this.defendantId = defendantId;
        this.hearingId = hearingId;
    }


    public UUID getWitnessId() {
        return witnessId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

}
