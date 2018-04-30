package uk.gov.moj.cpp.hearing.command.defendant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Address implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String postCode;

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

    public static Builder address() {
        return new Address.Builder();
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

        private String postcode;

        private Builder() {
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

        public Builder withPostcode(final String postcode) {
            this.postcode = postcode;
            return this;
        }

        public Address build() {
            return new Address(address1, address2, address3, address4, postcode);
        }
    }
}
