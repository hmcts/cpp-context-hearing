package uk.gov.moj.cpp.hearing.command.defendant;

import uk.gov.justice.core.courts.AssociatedPerson;
import uk.gov.justice.core.courts.DefendantAlias;
import uk.gov.justice.core.courts.LegalEntityDefendant;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.PersonDefendant;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Defendant implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private UUID prosecutionCaseId;

    private Integer numberOfPreviousConvictionsCited;

    private String prosecutionAuthorityReference;

    private String witnessStatement;

    private String witnessStatementWelsh;

    private String mitigation;

    private String mitigationWelsh;

    private List<AssociatedPerson> associatedPersons;

    private Organisation defenceOrganisation;

    private PersonDefendant personDefendant;

    private LegalEntityDefendant legalEntityDefendant;

    private List<DefendantAlias> aliases;

    private String pncId;

    private Boolean isYouth;

    public List<DefendantAlias> getAliases() {
        return aliases;
    }

    public void setAliases(List<DefendantAlias> aliases) {
        this.aliases = aliases;
    }

    public String getPncId() {
        return pncId;
    }

    public void setPncId(String pncId) {
        this.pncId = pncId;
    }

    public List<AssociatedPerson> getAssociatedPersons() {
        return associatedPersons;
    }

    public void setAssociatedPersons(List<AssociatedPerson> associatedPersons) {
        this.associatedPersons = associatedPersons;
    }

    public Organisation getDefenceOrganisation() {
        return defenceOrganisation;
    }

    public void setDefenceOrganisation(Organisation defenceOrganisation) {
        this.defenceOrganisation = defenceOrganisation;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LegalEntityDefendant getLegalEntityDefendant() {
        return legalEntityDefendant;
    }

    public void setLegalEntityDefendant(LegalEntityDefendant legalEntityDefendant) {
        this.legalEntityDefendant = legalEntityDefendant;
    }

    public String getMitigation() {
        return mitigation;
    }

    public void setMitigation(String mitigation) {
        this.mitigation = mitigation;
    }

    public String getMitigationWelsh() {
        return mitigationWelsh;
    }

    public void setMitigationWelsh(String mitigationWelsh) {
        this.mitigationWelsh = mitigationWelsh;
    }

    public Integer getNumberOfPreviousConvictionsCited() {
        return numberOfPreviousConvictionsCited;
    }

    public void setNumberOfPreviousConvictionsCited(Integer numberOfPreviousConvictionsCited) {
        this.numberOfPreviousConvictionsCited = numberOfPreviousConvictionsCited;
    }

    public PersonDefendant getPersonDefendant() {
        return personDefendant;
    }

    public void setPersonDefendant(PersonDefendant personDefendant) {
        this.personDefendant = personDefendant;
    }

    public String getProsecutionAuthorityReference() {
        return prosecutionAuthorityReference;
    }

    public void setProsecutionAuthorityReference(String prosecutionAuthorityReference) {
        this.prosecutionAuthorityReference = prosecutionAuthorityReference;
    }

    public UUID getProsecutionCaseId() {
        return prosecutionCaseId;
    }

    public void setProsecutionCaseId(UUID prosecutionCaseId) {
        this.prosecutionCaseId = prosecutionCaseId;
    }

    public String getWitnessStatement() {
        return witnessStatement;
    }

    public void setWitnessStatement(String witnessStatement) {
        this.witnessStatement = witnessStatement;
    }

    public String getWitnessStatementWelsh() {
        return witnessStatementWelsh;
    }

    public void setWitnessStatementWelsh(String witnessStatementWelsh) {
        this.witnessStatementWelsh = witnessStatementWelsh;
    }

    public Boolean getIsYouth() {
        return isYouth;
    }

    public void setIsYouth(final Boolean youth) {
        isYouth = youth;
    }
}
