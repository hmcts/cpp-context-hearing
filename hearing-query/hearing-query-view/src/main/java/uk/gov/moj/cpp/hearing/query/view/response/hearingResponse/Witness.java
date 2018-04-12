package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

public class Witness {
    private String id;
    private String caseId;
    private String type;
    private String classification;
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

    public Witness(Builder builder) {
        this.id = builder.id;
        this.caseId = builder.caseId;
        this.type = builder.type;
        this.classification = builder.classification;
        this.title = builder.title;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.dateOfBirth = builder.dateOfBirth;
        this.nationality = builder.nationality;
        this.gender = builder.gender;
        this.homeTelephone = builder.homeTelephone;
        this.workTelephone = builder.workTelephone;
        this.mobile = builder.mobile;
        this.fax = builder.fax;
        this.email = builder.email;
    }

    public String getId() {
        return id;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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

    public String getDateOfBirth() {
        return dateOfBirth;
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

    public String getNationality() {
        return nationality;
    }

    public String getGender() {
        return gender;
    }

    public String getWorkTelephone() {
        return workTelephone;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private String id;
        private String caseId;
        private String type;
        private String classification;
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

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withCaseId(String caseId) {
            this.caseId = caseId;
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

        public Builder withDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder withtType(String type) {
            this.type = type;
            return this;
        }

        public Builder withClassification(String classification) {
            this.classification = classification;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
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

        public Witness build() {
            return new Witness(this);

        }
    }
}
