package uk.gov.moj.cpp.hearing.persist.entity.ha;

import uk.gov.justice.json.schemas.core.DocumentationLanguageNeeds;
import uk.gov.justice.json.schemas.core.Gender;
import uk.gov.justice.json.schemas.core.Title;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ha_associated_person")
public class AssociatedPerson {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "defendant_id", insertable = false, updatable = false, referencedColumnName = "id"),
            @JoinColumn(name = "hearing_id", insertable = false, updatable = false, referencedColumnName = "hearing_id")})
    private Defendant defendant;

    @Column(name = "defendant_id")
    private UUID defendantId;

    @Column(name = "title")
    @Enumerated(EnumType.STRING)
    private Title title;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "nationality_id")
    private UUID nationalityId;

    @Column(name = "nationality_code")
    private String nationalityCode;

    @Column(name = "additional_nationality_id")
    private UUID additionalNationalityId;

    @Column(name = "additional_nationality_code")
    private String additionalNationalityCode;

    @Column(name = "disability_status")
    private String disabilityStatus;

    @Column(name = "ethnicity_id")
    private UUID ethnicityId;

    @Column(name = "ethnicity")
    private String ethnicity;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "interpreter_language_needs")
    private String interpreterLanguageNeeds;

    @Column(name = "documentation_language_needs")
    @Enumerated(EnumType.STRING)
    private DocumentationLanguageNeeds documentationLanguageNeeds;

    @Column(name = "national_insurance_number")
    private String nationalInsuranceNumber;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "occupation_code")
    private String occupationCode;

    @Column(name = "specific_requirements")
    private String specificRequirements;

    @AttributeOverrides({
            @AttributeOverride(name = "address1", column = @Column(name = "address_1")),
            @AttributeOverride(name = "address2", column = @Column(name = "address_2")),
            @AttributeOverride(name = "address3", column = @Column(name = "address_3")),
            @AttributeOverride(name = "address4", column = @Column(name = "address_4")),
            @AttributeOverride(name = "address5", column = @Column(name = "address_5")),
            @AttributeOverride(name = "postCode", column = @Column(name = "post_code"))
    })
    @Embedded
    private Address address;

    @AttributeOverrides({
            @AttributeOverride(name = "home", column = @Column(name = "contact_home")),
            @AttributeOverride(name = "work", column = @Column(name = "contact_work")),
            @AttributeOverride(name = "mobile", column = @Column(name = "contact_mobile")),
            @AttributeOverride(name = "primaryEmail", column = @Column(name = "contact_primary_email")),
            @AttributeOverride(name = "secondaryEmail", column = @Column(name = "contact_secondary_email")),
            @AttributeOverride(name = "fax", column = @Column(name = "contact_fax")),
    })
    @Embedded
    private Contact contact;

    @Column(name = "role")
    private String role;

    public AssociatedPerson() {
        //For JPA
    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    @Enumerated(EnumType.STRING)
    public Title getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public UUID getNationalityId() {
        return nationalityId;
    }

    public String getNationalityCode() {
        return nationalityCode;
    }

    public UUID getAdditionalNationalityId() {
        return additionalNationalityId;
    }

    public String getAdditionalNationalityCode() {
        return additionalNationalityCode;
    }

    public String getDisabilityStatus() {
        return disabilityStatus;
    }

    public UUID getEthnicityId() {
        return ethnicityId;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public Gender getGender() {
        return gender;
    }

    public String getInterpreterLanguageNeeds() {
        return interpreterLanguageNeeds;
    }

    public DocumentationLanguageNeeds getDocumentationLanguageNeeds() {
        return documentationLanguageNeeds;
    }

    public String getNationalInsuranceNumber() {
        return nationalInsuranceNumber;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getOccupationCode() {
        return occupationCode;
    }

    public String getSpecificRequirements() {
        return specificRequirements;
    }

    public Address getAddress() {
        return address;
    }

    public Contact getContact() {
        return contact;
    }

    public String getRole() {
        return role;
    }

    public void setId(HearingSnapshotKey id) {
        this.id = id;
    }

    public void setDefendant(Defendant defendant) {
        this.defendant = defendant;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setNationalityId(UUID nationalityId) {
        this.nationalityId = nationalityId;
    }

    public void setNationalityCode(String nationalityCode) {
        this.nationalityCode = nationalityCode;
    }

    public void setAdditionalNationalityId(UUID additionalNationalityId) {
        this.additionalNationalityId = additionalNationalityId;
    }

    public void setAdditionalNationalityCode(String additionalNationalityCode) {
        this.additionalNationalityCode = additionalNationalityCode;
    }

    public void setDisabilityStatus(String disabilityStatus) {
        this.disabilityStatus = disabilityStatus;
    }

    public void setEthnicityId(UUID ethnicityId) {
        this.ethnicityId = ethnicityId;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setInterpreterLanguageNeeds(String interpreterLanguageNeeds) {
        this.interpreterLanguageNeeds = interpreterLanguageNeeds;
    }

    public void setDocumentationLanguageNeeds(DocumentationLanguageNeeds documentationLanguageNeeds) {
        this.documentationLanguageNeeds = documentationLanguageNeeds;
    }

    public void setNationalInsuranceNumber(String nationalInsuranceNumber) {
        this.nationalInsuranceNumber = nationalInsuranceNumber;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public void setOccupationCode(String occupationCode) {
        this.occupationCode = occupationCode;
    }

    public void setSpecificRequirements(String specificRequirements) {
        this.specificRequirements = specificRequirements;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
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
        return Objects.equals(this.id, ((AssociatedPerson) o).id);
    }
}
