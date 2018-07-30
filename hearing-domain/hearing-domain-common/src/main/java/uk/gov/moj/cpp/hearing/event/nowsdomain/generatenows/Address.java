package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.util.UUID;

public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID addressId;

    private String address1;

    private String address2;

    private String address3;

    private String address4;

    private String postCode;

    public static Address address() {
        return new Address();
    }

    public UUID getAddressId() {
        return this.addressId;
    }

    public Address setAddressId(UUID addressId) {
        this.addressId = addressId;
        return this;
    }

    public String getAddress1() {
        return this.address1;
    }

    public Address setAddress1(String address1) {
        this.address1 = address1;
        return this;
    }

    public String getAddress2() {
        return this.address2;
    }

    public Address setAddress2(String address2) {
        this.address2 = address2;
        return this;
    }

    public String getAddress3() {
        return this.address3;
    }

    public Address setAddress3(String address3) {
        this.address3 = address3;
        return this;
    }

    public String getAddress4() {
        return this.address4;
    }

    public Address setAddress4(String address4) {
        this.address4 = address4;
        return this;
    }

    public String getPostCode() {
        return this.postCode;
    }

    public Address setPostCode(String postCode) {
        this.postCode = postCode;
        return this;
    }
}
