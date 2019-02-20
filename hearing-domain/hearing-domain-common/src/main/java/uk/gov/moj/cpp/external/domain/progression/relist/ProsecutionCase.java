package uk.gov.moj.cpp.external.domain.progression.relist;


import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ProsecutionCase implements Serializable {

    private static final long serialVersionUID = -4114340250045352801L;

    private UUID id;
    private List<Defendant> defendants;

    public ProsecutionCase() {
    }

    public ProsecutionCase(@JsonProperty(value = "id") final UUID id, @JsonProperty(value = "defendants") final List<Defendant> defendants) {
        this.id = id;
        this.defendants = defendants;
    }

    public static ProsecutionCase prosecutionCase() {
        return new ProsecutionCase();
    }

    public UUID getId() {
        return id;
    }

    public ProsecutionCase setId(UUID id) {
        this.id = id;
        return this;
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public ProsecutionCase setDefendants(List<Defendant> defendants) {
        this.defendants = defendants;
        return this;
    }
}
