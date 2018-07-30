package uk.gov.moj.cpp.external.domain.progression.relist;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = Include.NON_NULL)
@SuppressWarnings("squid:S1067")
public class Defendant implements Serializable{

    private static final long serialVersionUID = 5652031959552384288L;
    private final String id;

    private final List<Offence> offences;

    @JsonCreator
    public Defendant(@JsonProperty(value = "id") final String id,
                     @JsonProperty(value = "offences") final List<Offence> offences) {
        this.id = id;
        this.offences = offences;
    }

    public String getId() {
        return id;
    }

    public List<Offence> getOffences() {
        return offences;
    }

}
