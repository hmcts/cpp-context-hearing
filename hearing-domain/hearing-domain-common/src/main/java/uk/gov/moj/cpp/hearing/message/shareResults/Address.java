package uk.gov.moj.cpp.hearing.message.shareResults;

public class Address {

    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String postCode;

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

    public Address setAddress1(String address1) {
        this.address1 = address1;
        return this;
    }

    public Address setAddress2(String address2) {
        this.address2 = address2;
        return this;
    }

    public Address setAddress3(String address3) {
        this.address3 = address3;
        return this;
    }

    public Address setAddress4(String address4) {
        this.address4 = address4;
        return this;
    }

    public Address setPostCode(String postCode) {
        this.postCode = postCode;
        return this;
    }

    public static Address address() {
        return new Address();
    }
}
