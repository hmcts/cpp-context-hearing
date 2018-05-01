package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Witness implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final UUID caseId;
    private final String type;
    private final String classification;
    private final UUID personId;
    private final String title;
    private final String firstName;
    private final String lastName;
    private final String nationality;
    private final String gender;
    private final LocalDate dateOfBirth;
    private final String workTelephone;
    private final String homeTelephone;
    private final String mobile;
    private final String fax;
    private final String email;

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
                    @JsonProperty("email") final String email ) {
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

    public UUID getId() {
        return id;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getType() {
        return type;
    }

    public String getClassification() {
        return classification;
    }

    public UUID getPersonId() {
        return personId;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNationality() {
        return nationality;
    }

    public String getGender() {
        return gender;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getWorkTelephone() {
        return workTelephone;
    }

    public String getHomeTelephone() {
        return homeTelephone;
    }

    public String getMobile() {
        return mobile;
    }

    public String getFax() {
        return fax;
    }

    public String getEmail() {
        return email;
    }

    public static class Builder {

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

        private Builder() {

        }

       /* public UUID getId() {
            return id;
        }

        public UUID getPersonId() {
            return personId;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getNationality() {
            return nationality;
        }

        public String getGender() {
            return gender;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public UUID getCaseId() {
            return caseId;
        }

        public String getType() {
            return type;
        }

        public String getClassification() {
            return classification;
        }

        public String getTitle() {
            return title;
        }

        public String getWorkTelephone() {
            return workTelephone;
        }

        public String getHomeTelephone() {
            return homeTelephone;
        }

        public String getMobile() {
            return mobile;
        }

        public String getFax() {
            return fax;
        }

        public String getEmail() {
            return email;
        }*/

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withCaseId(UUID caseId){
            this.caseId = caseId;
            return this;
        }

        public Builder withType(String type){
            this.type = type;
            return this;
        }

        public Builder withClassification(String classification){
            this.classification = classification;
            return this;
        }

        public Builder withPersonId(UUID personId) {
            this.personId = personId;
            return this;
        }

        public Builder withTitle(String title){
            this.title = title;
            return this;
        }

        public Builder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withNationality(String nationality) {
            this.nationality = nationality;
            return this;
        }

        public Builder withGender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder withWorkTelephone(String workTelephone) {
            this.workTelephone = workTelephone;
            return this;
        }

        public Builder withDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder withHomeTelephone(String homeTelephone) {
            this.homeTelephone = homeTelephone;
            return this;
        }

        public Builder withMobile(String mobile) {
            this.mobile = mobile;
            return this;
        }

        public Builder withFax(String fax) {
            this.fax = fax;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Witness build() {
            return new Witness(id, caseId, type, classification, personId, title, firstName, lastName, nationality, gender,
                    dateOfBirth, homeTelephone, workTelephone, mobile, fax, email);
        }

    }

    public static Witness.Builder builder() {
        return new Builder();
    }

    public static Builder from(Witness witness) {
        Builder builder = builder()
                .withId(witness.getId())
                .withCaseId(witness.getCaseId())
                .withType(witness.getType())
                .withClassification(witness.getClassification())
                .withPersonId(witness.getPersonId())
                .withType(witness.getType())
                .withFirstName(witness.getFirstName())
                .withLastName(witness.getLastName())
                .withNationality(witness.getNationality())
                .withGender(witness.getGender())
                .withDateOfBirth(witness.getDateOfBirth())
                .withHomeTelephone(witness.getHomeTelephone())
                .withWorkTelephone(witness.getWorkTelephone())
                .withMobile(witness.getMobile())
                .withFax(witness.getFax())
                .withEmail(witness.getEmail());
        return builder;
    }

}