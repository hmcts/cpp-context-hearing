package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@SuppressWarnings("squid:S1067")
@Embeddable
public class ProsecutionCaseIdentifier {

    @Column(name = "prosecution_authority_id")
    private UUID prosecutionAuthorityId;

    @Column(name = "prosecution_authority_code")
    private String prosecutionAuthorityCode;

    @Column(name = "prosecution_authority_reference")
    private String prosecutionAuthorityReference;

    @Column(name = "caseurn")
    private String caseURN;

    public UUID getProsecutionAuthorityId() {
        return prosecutionAuthorityId;
    }

    public void setProsecutionAuthorityId(UUID prosecutionAuthorityId) {
        this.prosecutionAuthorityId = prosecutionAuthorityId;
    }

    public String getCaseURN() {
        return caseURN;
    }

    public void setCaseURN(String caseURN) {
        this.caseURN = caseURN;
    }

    public String getProsecutionAuthorityCode() {
        return prosecutionAuthorityCode;
    }

    public void setProsecutionAuthorityCode(String prosecutionAuthorityCode) {
        this.prosecutionAuthorityCode = prosecutionAuthorityCode;
    }

    public String getProsecutionAuthorityReference() {
        return prosecutionAuthorityReference;
    }

    public void setProsecutionAuthorityReference(String prosecutionAuthorityReference) {
        this.prosecutionAuthorityReference = prosecutionAuthorityReference;
    }
}
