package uk.gov.moj.cpp.hearing.common;

public class NameAddressReusableInformation {

    private String prisonOrganisationName;
    private String prisonOrganisationMiddleName;
    private String prisonOrganisationLastName;
    private String prisonOrganisationAddress1;
    private String prisonOrganisationAddress2;
    private String prisonOrganisationAddress3;
    private String prisonOrganisationAddress4;
    private String prisonOrganisationAddress5;
    private String prisonOrganisationPostCode;
    private String prisonOrganisationEmailAddress1;
    private String prisonOrganisationEmailAddress2;

    public NameAddressReusableInformation(final String prisonOrganisationName,
                                          final String prisonOrganisationMiddleName,
                                          final String prisonOrganisationLastName,
                                          final String prisonOrganisationAddress1,
                                          final String prisonOrganisationAddress2,
                                          final String prisonOrganisationAddress3,
                                          final String prisonOrganisationAddress4,
                                          final String prisonOrganisationAddress5,
                                          final String prisonOrganisationPostCode,
                                          final String prisonOrganisationEmailAddress1,
                                          final String prisonOrganisationEmailAddress2) {
        this.prisonOrganisationName = prisonOrganisationName;
        this.prisonOrganisationMiddleName = prisonOrganisationMiddleName;
        this.prisonOrganisationLastName = prisonOrganisationLastName;
        this.prisonOrganisationAddress1 = prisonOrganisationAddress1;
        this.prisonOrganisationAddress2 = prisonOrganisationAddress2;
        this.prisonOrganisationAddress3 = prisonOrganisationAddress3;
        this.prisonOrganisationAddress4 = prisonOrganisationAddress4;
        this.prisonOrganisationAddress5 = prisonOrganisationAddress5;
        this.prisonOrganisationPostCode = prisonOrganisationPostCode;
        this.prisonOrganisationEmailAddress1 = prisonOrganisationEmailAddress1;
        this.prisonOrganisationEmailAddress2 = prisonOrganisationEmailAddress2;
    }

    public String getPrisonOrganisationName() {
        return prisonOrganisationName;
    }

    public String getPrisonOrganisationMiddleName() {
        return prisonOrganisationMiddleName;
    }

    public String getPrisonOrganisationLastName() {
        return prisonOrganisationLastName;
    }

    public String getPrisonOrganisationAddress1() {
        return prisonOrganisationAddress1;
    }

    public String getPrisonOrganisationAddress2() {
        return prisonOrganisationAddress2;
    }

    public String getPrisonOrganisationAddress3() {
        return prisonOrganisationAddress3;
    }

    public String getPrisonOrganisationAddress4() {
        return prisonOrganisationAddress4;
    }

    public String getPrisonOrganisationAddress5() {
        return prisonOrganisationAddress5;
    }

    public String getPrisonOrganisationPostCode() {
        return prisonOrganisationPostCode;
    }

    public String getPrisonOrganisationEmailAddress1() {
        return prisonOrganisationEmailAddress1;
    }

    public String getPrisonOrganisationEmailAddress2() {
        return prisonOrganisationEmailAddress2;
    }

    public static class Builder {
        private String prisonOrganisationName;
        private String prisonOrganisationMiddleName;
        private String prisonOrganisationLastName;
        private String prisonOrganisationAddress1;
        private String prisonOrganisationAddress2;
        private String prisonOrganisationAddress3;
        private String prisonOrganisationAddress4;
        private String prisonOrganisationAddress5;
        private String prisonOrganisationPostCode;
        private String prisonOrganisationEmailAddress1;
        private String prisonOrganisationEmailAddress2;

        public NameAddressReusableInformation.Builder withPrisonOrganisationName(final String prisonOrganisationName) {
            this.prisonOrganisationName = prisonOrganisationName;
            return this;
        }

        public NameAddressReusableInformation.Builder withPrisonOrganisationMiddleName(final String prisonOrganisationMiddleName) {
            this.prisonOrganisationMiddleName = prisonOrganisationMiddleName;
            return this;
        }

        public NameAddressReusableInformation.Builder withPrisonOrganisationLastName(final String prisonOrganisationLastName) {
            this.prisonOrganisationLastName = prisonOrganisationLastName;
            return this;
        }

        public NameAddressReusableInformation.Builder withPrisonOrganisationAddress1(final String prisonOrganisationAddress1) {
            this.prisonOrganisationAddress1 = prisonOrganisationAddress1;
            return this;
        }

        public NameAddressReusableInformation.Builder withPrisonOrganisationAddress2(final String prisonOrganisationAddress2) {
            this.prisonOrganisationAddress2 = prisonOrganisationAddress2;
            return this;
        }

        public NameAddressReusableInformation.Builder withPrisonOrganisationAddress3(final String prisonOrganisationAddress3) {
            this.prisonOrganisationAddress3 = prisonOrganisationAddress3;
            return this;
        }

        public NameAddressReusableInformation.Builder withPrisonOrganisationAddress4(final String prisonOrganisationAddress4) {
            this.prisonOrganisationAddress4 = prisonOrganisationAddress4;
            return this;
        }

        public NameAddressReusableInformation.Builder withPrisonOrganisationAddress5(final String prisonOrganisationAddress5) {
            this.prisonOrganisationAddress5 = prisonOrganisationAddress5;
            return this;
        }

        public NameAddressReusableInformation.Builder withPrisonOrganisationPostCode(final String prisonOrganisationPostCode) {
            this.prisonOrganisationPostCode = prisonOrganisationPostCode;
            return this;
        }

        public NameAddressReusableInformation.Builder withPrisonOrganisationEmailAddress1(final String prisonOrganisationEmailAddress1) {
            this.prisonOrganisationEmailAddress1 = prisonOrganisationEmailAddress1;
            return this;
        }

        public NameAddressReusableInformation.Builder withPrisonOrganisationEmailAddress2(final String prisonOrganisationEmailAddress2) {
            this.prisonOrganisationEmailAddress2 = prisonOrganisationEmailAddress2;
            return this;
        }

        public NameAddressReusableInformation build() {
            return new NameAddressReusableInformation(prisonOrganisationName,
                    prisonOrganisationMiddleName,
                    prisonOrganisationLastName,
                    prisonOrganisationAddress1,
                    prisonOrganisationAddress2,
                    prisonOrganisationAddress3,
                    prisonOrganisationAddress4,
                    prisonOrganisationAddress5,
                    prisonOrganisationPostCode,
                    prisonOrganisationEmailAddress1,
                    prisonOrganisationEmailAddress2);
        }
    }
}
