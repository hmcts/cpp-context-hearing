package uk.gov.moj.cpp.hearing.persist.entity.ha;

import uk.gov.justice.core.courts.InitiationCode;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "ha_case")
public class ProsecutionCase {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Hearing hearing;

    @Column(name = "originating_organisation")
    private String originatingOrganisation;

    @Embedded
    private ProsecutionCaseIdentifier prosecutionCaseIdentifier;

    @Column(name = "initiation_code")
    @Enumerated(EnumType.STRING)
    private InitiationCode initiationCode;

    @Column(name = "case_status")
    private String caseStatus;

    @Column(name = "statement_of_facts")
    private String statementOfFacts;

    @Column(name = "statement_of_facts_welsh")
    private String statementOfFactsWelsh;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "prosecutionCase", orphanRemoval = true)
    private Set<Defendant> defendants = new HashSet<>();

    public ProsecutionCase() {
        //For JPA
    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public void setId(HearingSnapshotKey id) {
        this.id = id;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public void setHearing(Hearing hearing) {
        this.hearing = hearing;
    }

    public String getOriginatingOrganisation() {
        return originatingOrganisation;
    }

    public void setOriginatingOrganisation(String originatingOrganisation) {
        this.originatingOrganisation = originatingOrganisation;
    }

    public ProsecutionCaseIdentifier getProsecutionCaseIdentifier() {
        return prosecutionCaseIdentifier;
    }

    public void setProsecutionCaseIdentifier(ProsecutionCaseIdentifier prosecutionCaseIdentifier) {
        this.prosecutionCaseIdentifier = prosecutionCaseIdentifier;
    }

    public InitiationCode getInitiationCode() {
        return initiationCode;
    }

    public void setInitiationCode(InitiationCode initiationCode) {
        this.initiationCode = initiationCode;
    }

    public String getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(String caseStatus) {
        this.caseStatus = caseStatus;
    }

    public String getStatementOfFacts() {
        return statementOfFacts;
    }

    public void setStatementOfFacts(String statementOfFacts) {
        this.statementOfFacts = statementOfFacts;
    }

    public String getStatementOfFactsWelsh() {
        return statementOfFactsWelsh;
    }

    public void setStatementOfFactsWelsh(String statementOfFactsWelsh) {
        this.statementOfFactsWelsh = statementOfFactsWelsh;
    }

    public Set<Defendant> getDefendants() {
        return defendants;
    }

    public void setDefendants(Set<Defendant> defendants) {
        this.defendants = defendants;
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
        return Objects.equals(this.id, ((ProsecutionCase) o).id);
    }
}
