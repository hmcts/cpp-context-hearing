package uk.gov.moj.cpp.hearing.persist.entity.ex;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Address {
    @Column(name = "address_1")
    private String address1;

    @Column(name = "address_2")
    private String address2;

    @Column(name = "address_3")
    private String address3;

    @Column(name = "address_4")
    private String address4;

    @Column(name = "post_code")
    private String postCode;

    public Address(Builder builder) {
        this.address1 = builder.address1;
        this.address2 = builder.address2;
        this.address3 = builder.address3;
        this.address4 = builder.address4;
        this.postCode = builder.postCode;
    }

    public Address() {

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

        public Builder withAddress1(String address1) {
            this.address1=address1;
            return this;
        }

        public Builder withAddress2(String address2) {
            this.address2=address2;
            return this;
        }

        public Builder withAddress3(String address3) {
            this.address3=address3;
            return this;
        }

        public Builder withAddress4(String address4) {
            this.address4=address4;
            return this;
        }

        public Builder withPostCode(String postCode) {
            this.postCode=postCode;
            return this;
        }

        public Address build() {
            return new Address(this);
        }

    }



}
