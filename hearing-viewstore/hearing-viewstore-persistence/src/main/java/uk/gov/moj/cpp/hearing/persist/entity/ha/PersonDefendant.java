package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.time.LocalDate;

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

    @Column(name = "bail_status")
    private String bailStatus;

    @Column(name = "custody_time_limit")
    private LocalDate custodyTimeLimit;

    @Column(name = "driver_number")
    private String driverNumber;

    private Organisation employerOrganisation;

    @Column(name = "employer_payroll_reference")
    private String employerPayrollReference;

    @Column(name = "perceived_birth_year")
    private Integer perceivedBirthYear;

    @Embedded
    private Person personDetails;

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
}