package uk.gov.moj.cpp.hearing.command.initiate;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Witness implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID caseId;
    private String type;
    private String classification;
    private UUID personId;
    private String title;
    private String firstName;
    private String lastName;
    private String nationality;
    private String gender;
    private LocalDate dateOfBirth;
    private String workTelephone;
    private String homeTelephone;
    private String mobile;
    private String fax;
    private String email;

    public Witness() {
    }

    @JsonCreator
    private Witness(@JsonProperty("id") final UUID id,
                    @JsonProperty("caseId") final UUID caseId,
                    @JsonProperty("type") final String type,
                    @JsonProperty("classification") final String classification,
                    @JsonProperty("personId") final UUID personId,
                    @JsonProperty("title") final String title,
                    @JsonProperty("firstName") final String firstName,
                    @JsonProperty("lastName") final String lastName,
                    @JsonProperty("nationality") final String nationality,
                    @JsonProperty("gender") final String gender,
                    @JsonProperty("dateOfBirth") final LocalDate dateOfBirth,
                    @JsonProperty("homeTelephone") final String homeTelephone,
                    @JsonProperty("workTelephone") final String workTelephone,
                    @JsonProperty("mobile") final String mobile,
                    @JsonProperty("fax") final String fax,
                    @JsonProperty("email") final String email) {
        this.id = id;
        this.caseId = caseId;
        this.type = type;
        this.classification = classification;
        this.personId = personId;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationality = nationality;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.homeTelephone = homeTelephone;
        this.workTelephone = workTelephone;
        this.mobile = mobile;
        this.fax = fax;
        this.email = email;
    }

    public static Witness witness() {
        return new Witness();
    }

    public UUID getId() {
        return id;
    }

    public Witness setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public Witness setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public String getType() {
        return type;
    }

    public Witness setType(String type) {
        this.type = type;
        return this;
    }

    public String getClassification() {
        return classification;
    }

    public Witness setClassification(String classification) {
        this.classification = classification;
        return this;
    }

    public UUID getPersonId() {
        return personId;
    }

    public Witness setPersonId(UUID personId) {
        this.personId = personId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Witness setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public Witness setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public Witness setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getNationality() {
        return nationality;
    }

    public Witness setNationality(String nationality) {
        this.nationality = nationality;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public Witness setGender(String gender) {
        this.gender = gender;
        return this;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Witness setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public String getWorkTelephone() {
        return workTelephone;
    }

    public Witness setWorkTelephone(String workTelephone) {
        this.workTelephone = workTelephone;
        return this;
    }

    public String getHomeTelephone() {
        return homeTelephone;
    }

    public Witness setHomeTelephone(String homeTelephone) {
        this.homeTelephone = homeTelephone;
        return this;
    }

    public String getMobile() {
        return mobile;
    }

    public Witness setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getFax() {
        return fax;
    }

    public Witness setFax(String fax) {
        this.fax = fax;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Witness setEmail(String email) {
        this.email = email;
        return this;
    }

}