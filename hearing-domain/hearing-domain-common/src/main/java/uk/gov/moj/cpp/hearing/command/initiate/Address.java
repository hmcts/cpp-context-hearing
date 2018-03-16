package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Address {

    private final String address1;
    private final String address2;
    private final String address3;
    private final String address4;
    private final String postCode;

    @JsonCreator
    public Address(@JsonProperty("address1") final String address1,
                   @JsonProperty("address2") final String address2,
                   @JsonProperty("address3") final String address3,
                   @JsonProperty("address4") final String address4,
                   @JsonProperty("postCode") final String postCode) {
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.address4 = address4;
        this.postCode = postCode;
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

    public String getPostCode() {
        return postCode;
    }

    public static class Builder {

        private String address1;
        private String address2;
        private String address3;
        private String address4;
        private String postCode;

        private Builder() {

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

        public String getPostCode() {
            return postCode;
        }

        public Builder withAddress1(String address1) {
            this.address1 = address1;
            return this;
        }

        public Builder withAddress2(String address2) {
            this.address2 = address2;
            return this;
        }

        public Builder withAddress3(String address3) {
            this.address3 = address3;
            return this;
        }

        public Builder withAddress4(String address4) {
            this.address4 = address4;
            return this;
        }

        public Builder withPostCode(String postCode) {
            this.postCode = postCode;
            return this;
        }

        public Address build() {
            return new Address(address1, address2, address3, address4, postCode);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(Address address) {
        return builder()
                .withAddress1(address.getAddress1())
                .withAddress2(address.getAddress2())
                .withAddress3(address.getAddress3())
                .withAddress4(address.getAddress4())
                .withPostCode(address.getPostCode());
    }
}
