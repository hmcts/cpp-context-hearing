package uk.gov.moj.cpp.hearing.common;

public class AddressReusableInformation {

    private String parentguardiansaddressAddress1;
    private String parentguardiansaddressAddress2;
    private String parentguardiansaddressAddress3;
    private String parentguardiansaddressAddress4;
    private String parentguardiansaddressAddress5;
    private String parentguardiansaddressPostCode;
    private String parentguardiansaddressEmailAddress1;
    private String parentguardiansaddressEmailAddress2;

    public AddressReusableInformation(final String parentguardiansaddressAddress1,
                                      final String parentguardiansaddressAddress2,
                                      final String parentguardiansaddressAddress3,
                                      final String parentguardiansaddressAddress4,
                                      final String parentguardiansaddressAddress5,
                                      final String parentguardiansaddressPostCode,
                                      final String parentguardiansaddressEmailAddress1,
                                      final String parentguardiansaddressEmailAddress2) {
        this.parentguardiansaddressAddress1 = parentguardiansaddressAddress1;
        this.parentguardiansaddressAddress2 = parentguardiansaddressAddress2;
        this.parentguardiansaddressAddress3 = parentguardiansaddressAddress3;
        this.parentguardiansaddressAddress4 = parentguardiansaddressAddress4;
        this.parentguardiansaddressAddress5 = parentguardiansaddressAddress5;
        this.parentguardiansaddressPostCode = parentguardiansaddressPostCode;
        this.parentguardiansaddressEmailAddress1 = parentguardiansaddressEmailAddress1;
        this.parentguardiansaddressEmailAddress2 = parentguardiansaddressEmailAddress2;
    }

    public String getParentguardiansaddressAddress1() {
        return parentguardiansaddressAddress1;
    }

    public String getParentguardiansaddressAddress2() {
        return parentguardiansaddressAddress2;
    }

    public String getParentguardiansaddressAddress3() {
        return parentguardiansaddressAddress3;
    }

    public String getParentguardiansaddressAddress4() {
        return parentguardiansaddressAddress4;
    }

    public String getParentguardiansaddressAddress5() {
        return parentguardiansaddressAddress5;
    }

    public String getParentguardiansaddressPostCode() {
        return parentguardiansaddressPostCode;
    }

    public String getParentguardiansaddressEmailAddress1() {
        return parentguardiansaddressEmailAddress1;
    }

    public String getParentguardiansaddressEmailAddress2() {
        return parentguardiansaddressEmailAddress2;
    }

    public static class Builder {
        private String parentguardiansaddressAddress1;
        private String parentguardiansaddressAddress2;
        private String parentguardiansaddressAddress3;
        private String parentguardiansaddressAddress4;
        private String parentguardiansaddressAddress5;
        private String parentguardiansaddressPostCode;
        private String parentguardiansaddressEmailAddress1;
        private String parentguardiansaddressEmailAddress2;

        public AddressReusableInformation.Builder withParentguardiansaddressAddress1(final String parentguardiansaddressAddress1) {
            this.parentguardiansaddressAddress1 = parentguardiansaddressAddress1;
            return this;
        }

        public AddressReusableInformation.Builder withParentguardiansaddressAddress2(final String parentguardiansaddressAddress2) {
            this.parentguardiansaddressAddress2 = parentguardiansaddressAddress2;
            return this;
        }

        public AddressReusableInformation.Builder withParentguardiansaddressAddress3(final String parentguardiansaddressAddress3) {
            this.parentguardiansaddressAddress3 = parentguardiansaddressAddress3;
            return this;
        }

        public AddressReusableInformation.Builder withParentguardiansaddressAddress4(final String parentguardiansaddressAddress4) {
            this.parentguardiansaddressAddress4 = parentguardiansaddressAddress4;
            return this;
        }

        public AddressReusableInformation.Builder withParentguardiansaddressAddress5(final String parentguardiansaddressAddress5) {
            this.parentguardiansaddressAddress5 = parentguardiansaddressAddress5;
            return this;
        }

        public AddressReusableInformation.Builder withParentguardiansaddressPostCode(final String parentguardiansaddressPostCode) {
            this.parentguardiansaddressPostCode = parentguardiansaddressPostCode;
            return this;
        }

        public AddressReusableInformation.Builder withParentguardiansaddressEmailAddress1(final String parentguardiansaddressEmailAddress1) {
            this.parentguardiansaddressEmailAddress1 = parentguardiansaddressEmailAddress1;
            return this;
        }

        public AddressReusableInformation.Builder withParentguardiansaddressEmailAddress2(final String parentguardiansaddressEmailAddress2) {
            this.parentguardiansaddressEmailAddress2 = parentguardiansaddressEmailAddress2;
            return this;
        }

        public AddressReusableInformation build() {
            return new AddressReusableInformation(parentguardiansaddressAddress1,
                    parentguardiansaddressAddress2,
                    parentguardiansaddressAddress3,
                    parentguardiansaddressAddress4,
                    parentguardiansaddressAddress5,
                    parentguardiansaddressPostCode,
                    parentguardiansaddressEmailAddress1,
                    parentguardiansaddressEmailAddress2);
        }
    }
}
