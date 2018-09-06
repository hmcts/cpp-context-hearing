package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Address {

    private String formattedAddress;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String postCode;


    @JsonCreator
    private Address(@JsonProperty("formattedAddress") final String formattedAddress,
                    @JsonProperty("address1") final String address1,
                    @JsonProperty("address2") final String address2,
                    @JsonProperty("address3") final String address3,
                    @JsonProperty("address4") final String address4,
                    @JsonProperty("postCode") final String postCode) {
        this.formattedAddress = formattedAddress;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.address4 = address4;
        this.postCode = postCode;

    }

    public Address() {
    }

    private Address(final Builder builder) {
        this.formattedAddress = builder.formattedAddress;
        this.address1 = builder.address1;
        this.address2 = builder.address2;
        this.address3 = builder.address3;
        this.address4 = builder.address4;
        this.postCode = builder.postCode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getFormattedAddress() {
        return formattedAddress;
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

    public static final class Builder {

        private String formattedAddress;
        private String address1;
        private String address2;
        private String address3;
        private String address4;
        private String postCode;

        public Builder withFormattedAddress(final String formattedAddress) {
            this.formattedAddress = formattedAddress;
            return this;
        }

        public Builder withAddress1(final String address1) {
            this.address1 = address1;
            return this;
        }

        public Builder withAddress2(final String address2) {
            this.address2 = address2;
            return this;
        }

        public Builder withAddress3(final String address3) {
            this.address3 = address3;
            return this;
        }

        public Builder withAddress4(final String address4) {
            this.address4 = address4;
            return this;
        }

        public Builder withPostCode(final String postCode) {
            this.postCode = postCode;
            return this;
        }

        public Address build() {
            return new Address(this);
        }
    }
}