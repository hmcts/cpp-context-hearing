package uk.gov.moj.cpp.hearing.common;

public class NameAddressReusableInformationForApplication {

    private String organisationName;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String address5;
    private String postCode;
    private String primaryEmail;
    private String secondaryEmail;


    public NameAddressReusableInformationForApplication(final String organisationName,
            final String address1,
            final String address2,
            final String address3,
            final String address4,
            final String address5,
            final String postCode,
            final String primaryEmail,
            final String secondaryEmail){

        this.organisationName = organisationName;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.address4 = address4;
        this.address5 = address5;
        this.postCode = postCode;
        this.primaryEmail = primaryEmail;
        this.secondaryEmail = secondaryEmail;

    }

    public String getOrganisationName() {
        return organisationName;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getAddress3() {
        return address3;
    }

    public String getAddress4() {
        return address4;
    }

    public String getAddress5() {
        return address5;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public String getSecondaryEmail() {
        return secondaryEmail;
    }


    public static class Builder {
        private String organisationName;
        private String address1;
        private String address2;
        private String address3;
        private String address4;
        private String address5;
        private String postCode;
        private String primaryEmail;
        private String secondaryEmail;

        public NameAddressReusableInformationForApplication.Builder withOrganisationName(final String organisationName) {
            this.organisationName = organisationName;
            return this;
        }

        public NameAddressReusableInformationForApplication.Builder withAddress1(final String address1) {
            this.address1 = address1;
            return this;
        }

        public NameAddressReusableInformationForApplication.Builder withAddress2(final String address2) {
            this.address2 = address2;
            return this;
        }

        public NameAddressReusableInformationForApplication.Builder withAddress3(final String address3) {
            this.address3 = address3;
            return this;
        }

        public NameAddressReusableInformationForApplication.Builder withAddress4(final String address4) {
            this.address4 = address4;
            return this;
        }

        public NameAddressReusableInformationForApplication.Builder withAddress5(final String address5) {
            this.address5 = address5;
            return this;
        }

        public NameAddressReusableInformationForApplication.Builder withPostCode(final String postCode) {
            this.postCode = postCode;
            return this;
        }

        public NameAddressReusableInformationForApplication.Builder withPrimaryEmail(final String primaryEmail) {
            this.primaryEmail = primaryEmail;
            return this;
        }

        public NameAddressReusableInformationForApplication.Builder withSecondaryEmail(final String secondaryEmail) {
            this.secondaryEmail = secondaryEmail;
            return this;
        }

        public NameAddressReusableInformationForApplication build() {
            return new NameAddressReusableInformationForApplication(organisationName,
                    address1,
                    address2,
                    address3,
                    address4,
                    address5,
                    postCode,
                    primaryEmail,
                    secondaryEmail);
        }
    }
}
