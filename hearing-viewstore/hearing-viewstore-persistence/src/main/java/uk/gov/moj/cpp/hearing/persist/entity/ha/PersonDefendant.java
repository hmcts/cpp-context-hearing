package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import java.time.LocalDate;
import java.util.UUID;

@SuppressWarnings("squid:S1067")
@Embeddable
public class PersonDefendant {

    @Column(name = "aliases")
    private String aliases;

    @Column(name = "arrest_summons_number")
    private String arrestSummonsNumber;

    @Column(name = "bail_status")
    private String bailStatus;

    @Column(name = "custody_time_limit")
    private LocalDate custodyTimeLimit;

    @Column(name = "driver_number")
    private String driverNumber;

    private Organisation employerOrganisation;

    @Column(name = "employer_payroll_reference")
    private String employerPayrollReference;

    @Column(name = "observed_ethnicity_code")
    private String observedEthnicityCode;

    @Column(name = "observed_ethnicity_id")
    private UUID observedEthnicityId;

    @Column(name = "perceived_birth_year")
    private Integer perceivedBirthYear;

    @Embedded
    private Person personDetails;

    @Column(name = "p_nci_id")
    private String pncId;

    @Column(name = "self_defined_ethnicity_code")
    private String selfDefinedEthnicityCode;

    @Column(name = "self_defined_ethnicity_id")
    private UUID selfDefinedEthnicityId;

    public String getAliases() {
        return aliases;
    }

    public void setAliases(String aliases) {
        this.aliases = aliases;
    }

    public String getArrestSummonsNumber() {
        return arrestSummonsNumber;
    }

    public void setArrestSummonsNumber(String arrestSummonsNumber) {
        this.arrestSummonsNumber = arrestSummonsNumber;
    }

    public String getBailStatus() {
        return bailStatus;
    }

    public void setBailStatus(String bailStatus) {
        this.bailStatus = bailStatus;
    }

    public LocalDate getCustodyTimeLimit() {
        return custodyTimeLimit;
    }

    public void setCustodyTimeLimit(LocalDate custodyTimeLimit) {
        this.custodyTimeLimit = custodyTimeLimit;
    }

    public String getDriverNumber() {
        return driverNumber;
    }

    public void setDriverNumber(String driverNumber) {
        this.driverNumber = driverNumber;
    }

    public Organisation getEmployerOrganisation() {
        return employerOrganisation;
    }

    public void setEmployerOrganisation(Organisation employerOrganisation) {
        this.employerOrganisation = employerOrganisation;
    }

    public String getEmployerPayrollReference() {
        return employerPayrollReference;
    }

    public void setEmployerPayrollReference(String employerPayrollReference) {
        this.employerPayrollReference = employerPayrollReference;
    }

    public String getObservedEthnicityCode() {
        return observedEthnicityCode;
    }

    public void setObservedEthnicityCode(String observedEthnicityCode) {
        this.observedEthnicityCode = observedEthnicityCode;
    }

    public UUID getObservedEthnicityId() {
        return observedEthnicityId;
    }

    public void setObservedEthnicityId(UUID observedEthnicityId) {
        this.observedEthnicityId = observedEthnicityId;
    }

    public Integer getPerceivedBirthYear() {
        return perceivedBirthYear;
    }

    public void setPerceivedBirthYear(Integer perceivedBirthYear) {
        this.perceivedBirthYear = perceivedBirthYear;
    }

    public Person getPersonDetails() {
        return personDetails;
    }

    public void setPersonDetails(Person personDetails) {
        this.personDetails = personDetails;
    }

    public String getPncId() {
        return pncId;
    }

    public void setPncId(String pncId) {
        this.pncId = pncId;
    }

    public String getSelfDefinedEthnicityCode() {
        return selfDefinedEthnicityCode;
    }

    public void setSelfDefinedEthnicityCode(String selfDefinedEthnicityCode) {
        this.selfDefinedEthnicityCode = selfDefinedEthnicityCode;
    }

    public UUID getSelfDefinedEthnicityId() {
        return selfDefinedEthnicityId;
    }

    public void setSelfDefinedEthnicityId(UUID selfDefinedEthnicityId) {
        this.selfDefinedEthnicityId = selfDefinedEthnicityId;
    }
}