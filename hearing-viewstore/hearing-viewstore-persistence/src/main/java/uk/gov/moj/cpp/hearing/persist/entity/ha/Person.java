package uk.gov.moj.cpp.hearing.persist.entity.ha;


import uk.gov.justice.core.courts.Gender;
import uk.gov.justice.core.courts.HearingLanguage;

import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


@SuppressWarnings("squid:S1067")
@Embeddable
public class Person {

    @Column(name = "additional_nationality_code")
    private String additionalNationalityCode;

    @Column(name = "additional_nationality_id")
    private UUID additionalNationalityId;

    @Embedded
    private Address address;

    @Embedded
    private Contact contact;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "disability_status")
    private String disabilityStatus;

    @Column(name = "documentation_language_needs")
    @Enumerated(EnumType.STRING)
    private HearingLanguage documentationLanguageNeeds;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "interpreter_language_needs")
    private String interpreterLanguageNeeds;

    @Column(name = "national_insurance_number")
    private String nationalInsuranceNumber;

    @Column(name = "nationality_id")
    private UUID nationalityId;

    @Column(name = "nationality_code")
    private String nationalityCode;

    @Column(name = "nationality_description")
    private String nationalityDescription;


    @Column(name = "occupation")
    private String occupation;

    @Column(name = "occupation_code")
    private String occupationCode;

    @Column(name = "specific_requirements")
    private String specificRequirements;

    @Column(name = "title")
    private String title;

    @Embedded
    private Ethnicity ethnicity;

    public Person() {
        //For JPA
    }

    public String getAdditionalNationalityCode() {
        return additionalNationalityCode;
    }

    public void setAdditionalNationalityCode(String additionalNationalityCode) {
        this.additionalNationalityCode = additionalNationalityCode;
    }

    public UUID getAdditionalNationalityId() {
        return additionalNationalityId;
    }

    public void setAdditionalNationalityId(UUID additionalNationalityId) {
        this.additionalNationalityId = additionalNationalityId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDisabilityStatus() {
        return disabilityStatus;
    }

    public void setDisabilityStatus(String disabilityStatus) {
        this.disabilityStatus = disabilityStatus;
    }

    public HearingLanguage getDocumentationLanguageNeeds() {
        return documentationLanguageNeeds;
    }

    public void setDocumentationLanguageNeeds(HearingLanguage documentationLanguageNeeds) {
        this.documentationLanguageNeeds = documentationLanguageNeeds;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getInterpreterLanguageNeeds() {
        return interpreterLanguageNeeds;
    }

    public void setInterpreterLanguageNeeds(String interpreterLanguageNeeds) {
        this.interpreterLanguageNeeds = interpreterLanguageNeeds;
    }

    public String getNationalInsuranceNumber() {
        return nationalInsuranceNumber;
    }

    public void setNationalInsuranceNumber(String nationalInsuranceNumber) {
        this.nationalInsuranceNumber = nationalInsuranceNumber;
    }

    public UUID getNationalityId() {
        return nationalityId;
    }

    public void setNationalityId(UUID nationalityId) {
        this.nationalityId = nationalityId;
    }

    public String getNationalityCode() {
        return nationalityCode;
    }

    public void setNationalityCode(String nationalityCode) {
        this.nationalityCode = nationalityCode;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getOccupationCode() {
        return occupationCode;
    }

    public void setOccupationCode(String occupationCode) {
        this.occupationCode = occupationCode;
    }

    public String getSpecificRequirements() {
        return specificRequirements;
    }

    public void setSpecificRequirements(String specificRequirements) {
        this.specificRequirements = specificRequirements;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNationalityDescription() {
        return nationalityDescription;
    }

    public void setNationalityDescription(final String nationalityDescription) {
        this.nationalityDescription = nationalityDescription;
    }

    public Ethnicity getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(final Ethnicity ethnicity) {
        this.ethnicity = ethnicity;
    }
}