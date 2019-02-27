package uk.gov.moj.cpp.external.domain.progression.relist;


import uk.gov.justice.core.courts.JudicialRoleType;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class JudicialRole implements Serializable {

    private static final long serialVersionUID = 2973373200004567313L;

    private UUID judicialId;
    private JudicialRoleType judicialRoleType;

    public JudicialRole() {
    }

    public JudicialRole(@JsonProperty(value = "judicialId") final UUID judicialId, @JsonProperty(value = "judicialRoleType") final JudicialRoleType judicialRoleType) {
        this.judicialId = judicialId;
        this.judicialRoleType = judicialRoleType;
    }

    public static JudicialRole judicialRole() {
        return new JudicialRole();
    }

    public UUID getJudicialId() {
        return judicialId;
    }

    public JudicialRole setJudicialId(UUID judicialId) {
        this.judicialId = judicialId;
        return this;
    }

    public JudicialRoleType getJudicialRoleType() {
        return judicialRoleType;
    }

    public JudicialRole setJudicialRoleType(JudicialRoleType judicialRoleType) {
        this.judicialRoleType = judicialRoleType;
        return this;
    }
}
