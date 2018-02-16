package uk.gov.moj.cpp.hearing.command.verdict;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Defendant implements Serializable {
    private final UUID id;
    private final UUID personId;
    private final List<Offence> offences;

    @JsonCreator
    public Defendant(@JsonProperty("id") final UUID id,
                     @JsonProperty("personId") final UUID personId,
                     @JsonProperty("offences") final List<Offence> offences) {
        this.id = id;
        this.personId = personId;
        this.offences = (null == offences) ? new ArrayList<>() : new ArrayList<>(offences);

    }

    public UUID getPersonId() {
        return personId;
    }

    public UUID getId() {
        return id;
    }

    public List<Offence> getOffences() {
        return offences;
    }
}
