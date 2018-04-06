package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "formattedAddress",
        "address1",
        "address2",
        "address3",
        "address4",
        "postCode"
})
public class Address {
    private String formattedAddress;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String postCode;

    public String getformattedAddress() {
        return formattedAddress;
    }

    public void setformattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public Address withformattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
        return this;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public Address withAddress1(String address1) {
        this.address1 = address1;
        return this;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public Address withAddress2(String address2) {
        this.address2 = address2;
        return this;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public Address withAddress3(String address3) {
        this.address3 = address3;
        return this;
    }

    public String getAddress4() {
        return address4;
    }

    public void setAddress4(String address4) {
        this.address4 = address4;
    }

    public Address withAddress4(String address4) {
        this.address4 = address4;
        return this;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public Address withPostCode(String postCode) {
        this.postCode = postCode;
        return this;
    }
}
