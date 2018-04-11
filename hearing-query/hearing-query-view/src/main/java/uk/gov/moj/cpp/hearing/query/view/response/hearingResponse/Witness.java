package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "type",
        "classification",
        "personId",
        "title",
        "firstName",
        "lastName",
        "dateOfBirth",
        "nationality",
        "gender",
        "homeTelephone",
        "workTelephone",
        "mobile",
        "fax",
        "email"
})
public class Witness {
    private String id;
    private String caseId;
    private String type;
    private String classification;
    private String personId;
    private String title;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String nationality;
    private String gender;
    private String homeTelephone;
    private String workTelephone;
    private String mobile;
    private String fax;
    private String email;

    public String getPersonId() {
        return personId;
    }

    public Witness withPersonId(String personId) {
        this.personId = personId;
        return this;
    }

    public String getId() {
        return id;
    }

    public Witness withId(String id) {
        this.id = id;
        return this;
    }

    public String getCaseId() {
        return caseId;
    }

    public Witness withCaseId(String caseId) {
        this.caseId = caseId;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public Witness withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public Witness withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getHomeTelephone() {
        return homeTelephone;
    }

    public Witness withHomeTelephone(String homeTelephone) {
        this.homeTelephone = homeTelephone;
        return this;
    }

    public String getMobile() {
        return mobile;
    }

    public Witness withMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getFax() {
        return fax;
    }

    public Witness withFax(String fax) {
        this.fax = fax;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Witness withEmail(String email) {
        this.email = email;
        return this;
    }


    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public Witness withDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public String getType() {
        return type;
    }

    public Witness withtType(String type) {
        this.type = type;
        return this;
    }

    public String getClassification() {
        return classification;
    }

    public Witness withClassification(String classification) {
        this.classification = classification;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Witness withTitle(String title) {
        this.title = title;
        return this;
    }

    public String getNationality() {
        return nationality;
    }

    public Witness withNationality(String nationality) {
        this.nationality = nationality;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public Witness withGender(String gender) {
        this.gender = gender;
        return this;
    }

    public String getWorkTelephone() {
        return workTelephone;
    }

    public Witness withWorkTelephone(String workTelephone) {
        this.workTelephone = workTelephone;
        return this;
    }
}
