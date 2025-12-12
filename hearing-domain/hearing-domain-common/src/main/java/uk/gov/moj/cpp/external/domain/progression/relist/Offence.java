package uk.gov.moj.cpp.external.domain.progression.relist;


import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Offence implements Serializable {

    private static final long serialVersionUID = -3734514854083054415L;

    private UUID id;

    public Offence() {
    }

    public Offence(@JsonProperty(value = "id") final UUID id) {
        this.id = id;
    }

    public static Offence offence() {
        return new Offence();
    }

    public UUID getId() {
        return id;
    }

    public Offence setId(UUID id) {
        this.id = id;
        return this;
    }
}
