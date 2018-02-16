package uk.gov.moj.cpp.hearing.command.verdict;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public class Offence implements Serializable {
    private final UUID id;
    private final Verdict verdict;

    @JsonCreator
    public Offence(@JsonProperty("id") final UUID id,
                   @JsonProperty("verdict") final Verdict verdict) {
        this.id = id;
        this.verdict = verdict;

    }

    public UUID getId() {
        return id;
    }

    public Verdict getVerdict() {
        return verdict;
    }
}
