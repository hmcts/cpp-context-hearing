package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

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

    @Override
    public int hashCode() {
        return Objects.hash(this.prosecutionAuthorityCode, this.prosecutionAuthorityId, this.caseURN, this.prosecutionAuthorityReference);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        final ProsecutionCaseIdentifier c = (ProsecutionCaseIdentifier) o;
        return Objects.equals(this.prosecutionAuthorityCode, c.prosecutionAuthorityCode)
                && Objects.equals(this.prosecutionAuthorityId, c.prosecutionAuthorityId)
                && Objects.equals(this.caseURN, c.caseURN)
                && Objects.equals(this.prosecutionAuthorityReference, c.prosecutionAuthorityReference);
    }
}
