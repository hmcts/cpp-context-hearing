package uk.gov.moj.cpp.hearing.dto;


import java.time.LocalDate;
import java.util.UUID;

public class DefendantSearch {
    private UUID defendantId;
    private String forename;
    private String surname;
    private LocalDate dateOfBirth;
    private String legalEntityOrganizationName;
    private String nationalInsuranceNumber;

    public UUID getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
    }

    public String getForename() {
        return forename;
    }

    public String getSurname() {
        return  surname;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getLegalEntityOrganizationName() {
        return legalEntityOrganizationName;
    }

    public String getNationalInsuranceNumber() {
        return nationalInsuranceNumber;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setLegalEntityOrganizationName(String legalEntityOrganizationName) {
        this.legalEntityOrganizationName = legalEntityOrganizationName;
    }

    public void setNationalInsuranceNumber(String nationalInsuranceNumber) {
        this.nationalInsuranceNumber = nationalInsuranceNumber;
    }
}
