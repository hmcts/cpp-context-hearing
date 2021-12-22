package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "ha_defendant")
public class Defendant {

    @Id
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "prosecution_case_id", insertable = false, updatable = false, referencedColumnName = "id"),
            @JoinColumn(name = "hearing_id", insertable = false, updatable = false, referencedColumnName = "hearing_id")})
    private ProsecutionCase prosecutionCase;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "defendant", orphanRemoval = true)
    private Set<Offence> offences = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "defendant", orphanRemoval = true)
    private Set<AssociatedPerson> associatedPersons = new HashSet<>();

    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "defence_org_id")),
            @AttributeOverride(name = "name", column = @Column(name = "defence_org_name")),
            @AttributeOverride(name = "incorporationNumber", column = @Column(name = "defence_org_inc_no")),
            @AttributeOverride(name = "registeredCharityNumber", column = @Column(name = "defence_org_reg_charity_no")),
            @AttributeOverride(name = "address.address1", column = @Column(name = "defence_org_address_1")),
            @AttributeOverride(name = "address.address2", column = @Column(name = "defence_org_address_2")),
            @AttributeOverride(name = "address.address3", column = @Column(name = "defence_org_address_3")),
            @AttributeOverride(name = "address.address4", column = @Column(name = "defence_org_address_4")),
            @AttributeOverride(name = "address.address5", column = @Column(name = "defence_org_address_5")),
            @AttributeOverride(name = "address.postCode", column = @Column(name = "defence_org_post_code")),
            @AttributeOverride(name = "contact.home", column = @Column(name = "defence_org_contact_home")),
            @AttributeOverride(name = "contact.work", column = @Column(name = "defence_org_contact_work")),
            @AttributeOverride(name = "contact.mobile", column = @Column(name = "defence_org_contact_mobile")),
            @AttributeOverride(name = "contact.primaryEmail", column = @Column(name = "defence_org_contact_primary_email")),
            @AttributeOverride(name = "contact.secondaryEmail", column = @Column(name = "defence_org_contact_secondary_email")),
            @AttributeOverride(name = "contact.fax", column = @Column(name = "defence_org_contact_fax")),
    })
    @Embedded
    private Organisation defenceOrganisation;

    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "leg_ent_org_id")),
            @AttributeOverride(name = "name", column = @Column(name = "leg_ent_org_name")),
            @AttributeOverride(name = "incorporationNumber", column = @Column(name = "leg_ent_org_inc_no")),
            @AttributeOverride(name = "registeredCharityNumber", column = @Column(name = "leg_ent_org_reg_charity_no")),
            @AttributeOverride(name = "address.address1", column = @Column(name = "leg_ent_address_1")),
            @AttributeOverride(name = "address.address2", column = @Column(name = "leg_ent_address_2")),
            @AttributeOverride(name = "address.address3", column = @Column(name = "leg_ent_address_3")),
            @AttributeOverride(name = "address.address4", column = @Column(name = "leg_ent_address_4")),
            @AttributeOverride(name = "address.address5", column = @Column(name = "leg_ent_address_5")),
            @AttributeOverride(name = "address.postCode", column = @Column(name = "leg_ent_post_code")),
            @AttributeOverride(name = "contact.home", column = @Column(name = "leg_ent_contact_home")),
            @AttributeOverride(name = "contact.work", column = @Column(name = "leg_ent_contact_work")),
            @AttributeOverride(name = "contact.mobile", column = @Column(name = "leg_ent_contact_mobile")),
            @AttributeOverride(name = "contact.primaryEmail", column = @Column(name = "leg_ent_contact_primary_email")),
            @AttributeOverride(name = "contact.secondaryEmail", column = @Column(name = "leg_ent_contact_secondary_email")),
            @AttributeOverride(name = "contact.fax", column = @Column(name = "leg_ent_contact_fax")),
    })
    @Embedded
    private Organisation legalEntityOrganisation;

    @AttributeOverrides({
            @AttributeOverride(name = "employerOrganisation.id", column = @Column(name = "emp_org_id")),
            @AttributeOverride(name = "employerOrganisation.name", column = @Column(name = "emp_org_name")),
            @AttributeOverride(name = "employerOrganisation.incorporationNumber", column = @Column(name = "emp_org_inc_no")),
            @AttributeOverride(name = "employerOrganisation.registeredCharityNumber", column = @Column(name = "emp_org_reg_charity_no")),
            @AttributeOverride(name = "employerOrganisation.address.address1", column = @Column(name = "emp_org_address_1")),
            @AttributeOverride(name = "employerOrganisation.address.address2", column = @Column(name = "emp_org_address_2")),
            @AttributeOverride(name = "employerOrganisation.address.address3", column = @Column(name = "emp_org_address_3")),
            @AttributeOverride(name = "employerOrganisation.address.address4", column = @Column(name = "emp_org_address_4")),
            @AttributeOverride(name = "employerOrganisation.address.address5", column = @Column(name = "emp_org_address_5")),
            @AttributeOverride(name = "employerOrganisation.address.postCode", column = @Column(name = "emp_org_post_code")),
            @AttributeOverride(name = "employerOrganisation.contact.home", column = @Column(name = "emp_org_contact_home")),
            @AttributeOverride(name = "employerOrganisation.contact.work", column = @Column(name = "emp_org_contact_work")),
            @AttributeOverride(name = "employerOrganisation.contact.mobile", column = @Column(name = "emp_org_contact_mobile")),
            @AttributeOverride(name = "employerOrganisation.contact.primaryEmail", column = @Column(name = "emp_org_contact_primary_email")),
            @AttributeOverride(name = "employerOrganisation.contact.secondaryEmail", column = @Column(name = "emp_org_contact_secondary_email")),
            @AttributeOverride(name = "employerOrganisation.contact.fax", column = @Column(name = "emp_org_contact_fax")),
            @AttributeOverride(name = "personDetails.address.address1", column = @Column(name = "defendant_address_1")),
            @AttributeOverride(name = "personDetails.address.address2", column = @Column(name = "defendant_address_2")),
            @AttributeOverride(name = "personDetails.address.address3", column = @Column(name = "defendant_address_3")),
            @AttributeOverride(name = "personDetails.address.address4", column = @Column(name = "defendant_address_4")),
            @AttributeOverride(name = "personDetails.address.address5", column = @Column(name = "defendant_address_5")),
            @AttributeOverride(name = "personDetails.address.postCode", column = @Column(name = "defendant_post_code")),
            @AttributeOverride(name = "personDetails.contact.home", column = @Column(name = "defendant_contact_home")),
            @AttributeOverride(name = "personDetails.contact.work", column = @Column(name = "defendant_contact_work")),
            @AttributeOverride(name = "personDetails.contact.mobile", column = @Column(name = "defendant_contact_mobile")),
            @AttributeOverride(name = "personDetails.contact.primaryEmail", column = @Column(name = "defendant_contact_primary_email")),
            @AttributeOverride(name = "personDetails.contact.secondaryEmail", column = @Column(name = "defendant_contact_secondary_email")),
            @AttributeOverride(name = "personDetails.contact.fax", column = @Column(name = "defendant_contact_fax")),
            @AttributeOverride(name = "custodialEstablishment.custody", column = @Column(name = "custodial_establishment_custody")),
            @AttributeOverride(name = "custodialEstablishment.id", column = @Column(name = "custodial_establishment_id")),
            @AttributeOverride(name = "custodialEstablishment.name", column = @Column(name = "custodial_establishment_name"))
    })
    @Embedded
    private PersonDefendant personDefendant;
    @AttributeOverrides({
            @AttributeOverride(name = "defenceOrganisation.name", column = @Column(name = "org_name")),
            @AttributeOverride(name = "defenceOrganisation.incorporationNumber", column = @Column(name = "org_inc_no")),
            @AttributeOverride(name = "defenceOrganisation.registeredCharityNumber", column = @Column(name = "org_reg_charity_no")),
            @AttributeOverride(name = "defenceOrganisation.address.address1", column = @Column(name = "associate_org_address_1")),
            @AttributeOverride(name = "defenceOrganisation.address.address2", column = @Column(name = "associate_org_address_2")),
            @AttributeOverride(name = "defenceOrganisation.address.address3", column = @Column(name = "associate_org_address_3")),
            @AttributeOverride(name = "defenceOrganisation.address.address4", column = @Column(name = "associate_org_address_4")),
            @AttributeOverride(name = "defenceOrganisation.address.address5", column = @Column(name = "associate_org_address_5")),
            @AttributeOverride(name = "defenceOrganisation.address.postCode", column = @Column(name = "associate_org_post_code")),
            @AttributeOverride(name = "defenceOrganisation.contact.home", column = @Column(name = "associate_org_contact_home")),
            @AttributeOverride(name = "defenceOrganisation.contact.work", column = @Column(name = "associate_org_contact_work")),
            @AttributeOverride(name = "defenceOrganisation.contact.mobile", column = @Column(name = "associate_org_contact_mobile")),
            @AttributeOverride(name = "defenceOrganisation.contact.primaryEmail", column = @Column(name = "associate_org_contact_primary_email")),
            @AttributeOverride(name = "defenceOrganisation.contact.secondaryEmail", column = @Column(name = "associate_org_contact_secondary_email")),
            @AttributeOverride(name = "defenceOrganisation.contact.fax", column = @Column(name = "associate_org_contact_fax")),
    })
    @Embedded
    private AssociatedDefenceOrganisation associatedDefenceOrganisation;

    @Column(name = "prosecution_case_id")
    private UUID prosecutionCaseId;

    @Column(name = "master_defendant_id")
    private UUID masterDefendantId;

    @Column(name = "court_proceedings_initiated")
    private ZonedDateTime courtProceedingsInitiated;

    @Column(name = "number_of_previous_convictions_cited")
    private Integer numberOfPreviousConvictionsCited;

    @Column(name = "prosecution_authority_reference")
    private String prosecutionAuthorityReference;

    @Column(name = "witness_statement")
    private String witnessStatement;

    @Column(name = "witness_statement_welsh")
    private String witnessStatementWelsh;

    @Column(name = "mitigation")
    private String mitigation;

    @Column(name = "mitigation_welsh")
    private String mitigationWelsh;

    @Column(name = "p_nci_id")
    private String pncId;

    @Column(name = "is_youth")
    private Boolean isYouth;

    @Column(name = "legalaid_status")
    private String legalaidStatus;

    @Column(name = "proceedingsConcluded")
    private boolean proceedingsConcluded;

    @Column(name = "is_court_list_restricted")
    private Boolean isCourtListRestricted;

    public Defendant() {
        //For JPA
    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public void setId(final HearingSnapshotKey id) {
        this.id = id;
    }

    public ProsecutionCase getProsecutionCase() {
        return prosecutionCase;
    }

    public void setProsecutionCase(final ProsecutionCase prosecutionCase) {
        this.prosecutionCase = prosecutionCase;
    }

    public Set<Offence> getOffences() {
        return offences;
    }

    public void setOffences(final Set<Offence> offences) {
        this.offences = offences;
    }

    public Set<AssociatedPerson> getAssociatedPersons() {
        return associatedPersons;
    }

    public void setAssociatedPersons(final Set<AssociatedPerson> associatedPersons) {
        this.associatedPersons = associatedPersons;
    }

    public Organisation getDefenceOrganisation() {
        return defenceOrganisation;
    }

    public void setDefenceOrganisation(final Organisation defenceOrganisation) {
        this.defenceOrganisation = defenceOrganisation;
    }

    public Organisation getLegalEntityOrganisation() {
        return legalEntityOrganisation;
    }

    public void setLegalEntityOrganisation(final Organisation legalEntityOrganisation) {
        this.legalEntityOrganisation = legalEntityOrganisation;
    }

    public PersonDefendant getPersonDefendant() {
        return personDefendant;
    }

    public void setPersonDefendant(final PersonDefendant personDefendant) {
        this.personDefendant = personDefendant;
    }

    public UUID getProsecutionCaseId() {
        return prosecutionCaseId;
    }

    public void setProsecutionCaseId(final UUID prosecutionCaseId) {
        this.prosecutionCaseId = prosecutionCaseId;
    }

    public Integer getNumberOfPreviousConvictionsCited() {
        return numberOfPreviousConvictionsCited;
    }

    public void setNumberOfPreviousConvictionsCited(final Integer numberOfPreviousConvictionsCited) {
        this.numberOfPreviousConvictionsCited = numberOfPreviousConvictionsCited;
    }

    public String getProsecutionAuthorityReference() {
        return prosecutionAuthorityReference;
    }

    public void setProsecutionAuthorityReference(final String prosecutionAuthorityReference) {
        this.prosecutionAuthorityReference = prosecutionAuthorityReference;
    }

    public String getWitnessStatement() {
        return witnessStatement;
    }

    public void setWitnessStatement(final String witnessStatement) {
        this.witnessStatement = witnessStatement;
    }

    public String getWitnessStatementWelsh() {
        return witnessStatementWelsh;
    }

    public void setWitnessStatementWelsh(final String witnessStatementWelsh) {
        this.witnessStatementWelsh = witnessStatementWelsh;
    }

    public String getMitigation() {
        return mitigation;
    }

    public void setMitigation(final String mitigation) {
        this.mitigation = mitigation;
    }

    public String getMitigationWelsh() {
        return mitigationWelsh;
    }

    public void setMitigationWelsh(final String mitigationWelsh) {
        this.mitigationWelsh = mitigationWelsh;
    }

    public String getPncId() {
        return pncId;
    }

    public void setPncId(final String pncId) {
        this.pncId = pncId;
    }

    public Boolean getIsYouth() {
        return isYouth;
    }

    public void setIsYouth(final Boolean youth) {
        isYouth = youth;
    }

    public String getLegalaidStatus() {
        return legalaidStatus;
    }

    public void setLegalaidStatus(String legalaidStatus) {
        this.legalaidStatus = legalaidStatus;
    }

    public boolean isProceedingsConcluded() {
        return proceedingsConcluded;
    }

    public void setProceedingsConcluded(boolean proceedingsConcluded) {
        this.proceedingsConcluded = proceedingsConcluded;
    }

    public AssociatedDefenceOrganisation getAssociatedDefenceOrganisation() {
        return associatedDefenceOrganisation;
    }

    public void setAssociatedDefenceOrganisation(final AssociatedDefenceOrganisation associatedDefenceOrganisation) {
        this.associatedDefenceOrganisation = associatedDefenceOrganisation;
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
        return Objects.equals(this.id, ((Defendant) o).id);
    }

    public UUID getMasterDefendantId() {
        return masterDefendantId;
    }

    public void setMasterDefendantId(final UUID masterDefendantId) {
        this.masterDefendantId = masterDefendantId;
    }

    public ZonedDateTime getCourtProceedingsInitiated() {
        return courtProceedingsInitiated;
    }

    public void setCourtProceedingsInitiated(final ZonedDateTime courtProceedingsInitiated) {
        this.courtProceedingsInitiated = courtProceedingsInitiated;
    }

    public Boolean getCourtListRestricted() {
        return isCourtListRestricted;
    }

    public void setCourtListRestricted(Boolean courtListRestricted) {
        isCourtListRestricted = courtListRestricted;
    }
}