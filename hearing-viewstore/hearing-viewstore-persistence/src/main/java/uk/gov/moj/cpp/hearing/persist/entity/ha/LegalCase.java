package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ha_case")
public class LegalCase {

    @Id
    @Column(name = "id", nullable = false)
    private java.util.UUID id;

    @Column(name = "caseurn")
    private String caseurn;

    public LegalCase(Builder builder) {
        this.id = builder.id;
        this.caseurn = builder.caseurn;
    }

    public LegalCase() {

    }

    public java.util.UUID getId() {
        return id;
    }

    public String getCaseUrn() {
        return caseurn;
    }

    public void setCaseurn(String caseurn) {
        this.caseurn=caseurn;
    }

    public static class Builder {

        private java.util.UUID id;

        private String caseurn;

        private Builder() {}

        public Builder withId(final java.util.UUID id) {
            this.id = id;
            return this;
        }

        public Builder withCaseurn(final String caseurn) {
            this.caseurn = caseurn;
            return this;
        }

        public LegalCase build() {
            return new LegalCase(this);
        }


    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.id, ((LegalCase)o).id);
    }
}