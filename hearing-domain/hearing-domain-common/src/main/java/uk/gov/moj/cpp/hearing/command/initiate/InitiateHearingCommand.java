package uk.gov.moj.cpp.hearing.command.initiate;

import uk.gov.justice.core.courts.Hearing;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InitiateHearingCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private Hearing hearing;

    public InitiateHearingCommand() {
    }

    @JsonCreator
    public InitiateHearingCommand(@JsonProperty("hearing") final Hearing hearing) {
        this.hearing = hearing;
    }

    public static InitiateHearingCommand initiateHearingCommand() {
        return new InitiateHearingCommand();
    }

    public Hearing getHearing() {
        return hearing;
    }

    public InitiateHearingCommand setHearing(Hearing hearing) {
        this.hearing = hearing;
        return this;
    }
}
