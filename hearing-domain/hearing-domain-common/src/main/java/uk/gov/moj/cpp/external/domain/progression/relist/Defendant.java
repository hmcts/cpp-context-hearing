package uk.gov.moj.cpp.external.domain.progression.relist;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@JsonInclude(value = Include.NON_NULL)
@SuppressWarnings("squid:S1067")
public class Defendant implements Serializable {

    private static final long serialVersionUID = 5652031959552384288L;

    private UUID id;
    private List<Offence> offences;

    public Defendant() {
    }

    @JsonCreator
    public Defendant(@JsonProperty(value = "id") final UUID id,
                     @JsonProperty(value = "offences") final List<Offence> offences) {
        this.id = id;
        this.offences = offences;
    }

    public UUID getId() {
        return id;
    }

    public Defendant setId(UUID id) {
        this.id = id;
        return this;
    }

    public List<Offence> getOffences() {
        return offences;
    }

    public Defendant setOffences(List<Offence> offences) {
        this.offences = offences;
        return this;
    }

    public static Defendant defendant() {
        return new Defendant();
    }
}
