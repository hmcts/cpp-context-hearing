package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

import java.io.Serializable;

public class InitiateHearingCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Case> cases;
    private Hearing hearing;

    public InitiateHearingCommand() {
    }

    @JsonCreator
    public InitiateHearingCommand(@JsonProperty("cases") List<Case> cases,
                                  @JsonProperty("hearing") final Hearing hearing) {
        this.cases = cases;
        this.hearing = hearing;
    }

    public List<Case> getCases() {
        return cases;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public InitiateHearingCommand setCases(List<Case> cases) {
        this.cases = new ArrayList<>(cases);
        return this;
    }

    public InitiateHearingCommand setHearing(Hearing hearing) {
        this.hearing = hearing;
        return this;
    }

    public static InitiateHearingCommand initiateHearingCommand() {
        return new InitiateHearingCommand();
    }
}
