package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@SuppressWarnings("squid:S1067")
@Embeddable
public class PersonDefendant {

    @Column(name = "aliases")
    private String aliases;

    @Column(name = "arrest_summons_number")
    private String arrestSummonsNumber;

    @Column(name = "bail_status_desc")
    private String bailStatusDesc;

    @Column(name = "custody_time_limit")
    private LocalDate custodyTimeLimit;

    @Column(name = "driver_number")
    private String driverNumber;

    private Organisation employerOrganisation;

    @Column(name = "employer_payroll_reference")
    private String employerPayrollReference;

    @Column(name = "perceived_birth_year")
    private Integer perceivedBirthYear;

    @Column(name = "bail_status_id")
    private UUID bailStatusId;

    @Column(name = "bail_status_code")
    private String bailStatusCode;

    @Embedded
    private Person personDetails;

    @Embedded
    private CustodialEstablishment custodialEstablishment;

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

    public String getBailStatusDesc() {
        return bailStatusDesc;
    }

    public void setBailStatusDesc(String bailStatus) {
        this.bailStatusDesc = bailStatus;
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

    public UUID getBailStatusId() {
        return bailStatusId;
    }

    public void setBailStatusId(UUID bailStatusId) {
        this.bailStatusId = bailStatusId;
    }

    public String getBailStatusCode() {
        return bailStatusCode;
    }

    public void setBailStatusCode(String bailStatusCode) {
        this.bailStatusCode = bailStatusCode;
    }

    public CustodialEstablishment getCustodialEstablishment() {
        return custodialEstablishment;
    }

    public void setCustodialEstablishment(final CustodialEstablishment custodialEstablishment) {
        this.custodialEstablishment = custodialEstablishment;
    }
}