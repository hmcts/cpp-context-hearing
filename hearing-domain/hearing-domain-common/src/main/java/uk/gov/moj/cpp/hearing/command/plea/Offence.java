package uk.gov.moj.cpp.hearing.command.plea;


import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Offence implements Serializable {
    private final UUID id;
    private final Plea plea;

    @JsonCreator
    public Offence(@JsonProperty("id") final UUID id,
                   @JsonProperty("plea") final Plea plea) {
        this.id = id;
        this.plea = plea;

    }

    public UUID getId() {
        return id;
    }

    public Plea getPlea() {
        return plea;
    }
}
