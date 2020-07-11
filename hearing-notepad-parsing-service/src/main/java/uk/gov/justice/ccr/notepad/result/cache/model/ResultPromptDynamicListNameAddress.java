package uk.gov.justice.ccr.notepad.result.cache.model;


import java.util.Objects;

public class ResultPromptDynamicListNameAddress {

    private String name;
    private String firstName;
    private String middleName;
    private String lastName;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String addressLine4;
    private String addressLine5;
    private String postcode;
    private String emailAddress1;
    private String emailAddress2;

    public ResultPromptDynamicListNameAddress() {

    }

    public ResultPromptDynamicListNameAddress(final String name, final String firstName, final String middleName,
                                              final String lastName, final String addressLine1, final String addressLine2,
                                              final String addressLine3, final String addressLine4, final String addressLine5,
                                              final String postcode, final String emailAddress1, final String emailAddress2) {
        this.name = name;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressLine3 = addressLine3;
        this.addressLine4 = addressLine4;
        this.addressLine5 = addressLine5;
        this.postcode = postcode;
        this.emailAddress1 = emailAddress1;
        this.emailAddress2 = emailAddress2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResultPromptDynamicListNameAddress that = (ResultPromptDynamicListNameAddress) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getAddressLine1(), that.getAddressLine1()) &&
                Objects.equals(getEmailAddress1(), that.getEmailAddress1());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getName(), getFirstName(), getMiddleName(), getLastName(), getAddressLine1(), getAddressLine2(), getAddressLine3(), getAddressLine4(), getAddressLine5(), getPostcode(), getEmailAddress1(), getEmailAddress2());
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public String getAddressLine4() {
        return addressLine4;
    }

    public void setAddressLine4(String addressLine4) {
        this.addressLine4 = addressLine4;
    }

    public String getAddressLine5() {
        return addressLine5;
    }

    public void setAddressLine5(String addressLine5) {
        this.addressLine5 = addressLine5;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getEmailAddress1() {
        return emailAddress1;
    }

    public void setEmailAddress1(String emailAddress1) {
        this.emailAddress1 = emailAddress1;
    }

    public String getEmailAddress2() {
        return emailAddress2;
    }

    public void setEmailAddress2(String emailAddress2) {
        this.emailAddress2 = emailAddress2;
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

    @Override
    public String toString() {
        return "ResultPromptDynamicListNameAddress{" +
                "name='" + name + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", addressLine1='" + addressLine1 + '\'' +
                ", addressLine2='" + addressLine2 + '\'' +
                ", addressLine3='" + addressLine3 + '\'' +
                ", addressLine4='" + addressLine4 + '\'' +
                ", addressLine5='" + addressLine5 + '\'' +
                ", postcode='" + postcode + '\'' +
                ", emailAddress1='" + emailAddress1 + '\'' +
                ", emailAddress2='" + emailAddress2 + '\'' +
                '}';
    }

    public static ResultPromptDynamicListNameAddressBuilder resultPromptDynamicListNameAddressBuilder() {
        return new ResultPromptDynamicListNameAddressBuilder();
    }


    public static class ResultPromptDynamicListNameAddressBuilder {

        private String name;
        private String firstName;
        private String middleName;
        private String lastName;
        private String addressLine1;
        private String addressLine2;
        private String addressLine3;
        private String addressLine4;
        private String addressLine5;
        private String postcode;
        private String emailAddress1;
        private String emailAddress2;

        public ResultPromptDynamicListNameAddress.ResultPromptDynamicListNameAddressBuilder withName(final String name) {
            this.name = name;
            return this;
        }

        public ResultPromptDynamicListNameAddress.ResultPromptDynamicListNameAddressBuilder withFirstName(final String firstName) {
            this.firstName = firstName;
            return this;
        }

        public ResultPromptDynamicListNameAddress.ResultPromptDynamicListNameAddressBuilder withMiddleName(final String middleName) {
            this.middleName = middleName;
            return this;
        }

        public ResultPromptDynamicListNameAddress.ResultPromptDynamicListNameAddressBuilder withLastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        public ResultPromptDynamicListNameAddress.ResultPromptDynamicListNameAddressBuilder withAddressLine1(final String addressLine1) {
            this.addressLine1 = addressLine1;
            return this;
        }

        public ResultPromptDynamicListNameAddress.ResultPromptDynamicListNameAddressBuilder withAddressLine2(final String addressLine2) {
            this.addressLine2 = addressLine2;
            return this;
        }

        public ResultPromptDynamicListNameAddress.ResultPromptDynamicListNameAddressBuilder withAddressLine3(final String addressLine3) {
            this.addressLine3 = addressLine3;
            return this;
        }

        public ResultPromptDynamicListNameAddress.ResultPromptDynamicListNameAddressBuilder withAddressLine4(final String withAddressLine4) {
            this.addressLine4 = withAddressLine4;
            return this;
        }

        public ResultPromptDynamicListNameAddress.ResultPromptDynamicListNameAddressBuilder withAddressLine5(final String addressLine5) {
            this.addressLine5 = addressLine5;
            return this;
        }

        public ResultPromptDynamicListNameAddress.ResultPromptDynamicListNameAddressBuilder withPostCode(final String postCode) {
            this.postcode = postCode;
            return this;
        }

        public ResultPromptDynamicListNameAddress.ResultPromptDynamicListNameAddressBuilder withEmailAddress1(final String emailAddress1) {
            this.emailAddress1 = emailAddress1;
            return this;
        }

        public ResultPromptDynamicListNameAddress.ResultPromptDynamicListNameAddressBuilder withEmailAddress2(final String emailAddress2) {
            this.emailAddress2 = emailAddress2;
            return this;
        }

        public ResultPromptDynamicListNameAddress build() {
            return new ResultPromptDynamicListNameAddress(name, firstName, middleName, lastName, addressLine1, addressLine2,
                    addressLine3, addressLine4, addressLine5, postcode, emailAddress1, emailAddress2);
        }
    }
}
