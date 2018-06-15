package uk.gov.moj.cpp.hearing.command.initiate;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String postCode;

    public Address() {
    }

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
