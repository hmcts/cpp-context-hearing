package uk.gov.moj.cpp.hearing.command.hearing.details;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class HearingAddWitnessCommand {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;
    private final String witness;

    @JsonCreator
    public HearingAddWitnessCommand(@JsonProperty("hearingId") final UUID hearingId,
                                    @JsonProperty("witness") final String newWitness) {
        this.hearingId = hearingId;
        this.witness = newWitness;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public String getWitness() {
        return witness;
    }
}

