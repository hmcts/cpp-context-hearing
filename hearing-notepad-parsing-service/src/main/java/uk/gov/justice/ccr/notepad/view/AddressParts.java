package uk.gov.justice.ccr.notepad.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressParts implements Serializable {

    private static final long serialVersionUID = 2186548466720532801L;

    private String name;

    private String  firstName;

    private String  middleName;

    private String lastName;

    private String address1;

    private String address2;

    private String address3;

    private String address4;

    private String address5;

    private String postCode;

    private String email1;

    private String email2;

    @JsonCreator
    private AddressParts(@JsonProperty("name") final String name, @JsonProperty("firstName") final String firstName,
                         @JsonProperty("middleName") final String middleName, @JsonProperty("lastName") final String lastName,
                         @JsonProperty("address1") final String address1, @JsonProperty("address2") final String address2,
                         @JsonProperty("address3") final String address3, @JsonProperty("address4") final String address4,
                         @JsonProperty("address5") final String address5, @JsonProperty("postcode") final String postCode,
                         @JsonProperty("email1") final String email1, @JsonProperty("email2") final String email2) {
        this.name = name;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 =address3;
        this.address4 = address4;
        this.address5 = address5;
        this.postCode = postCode;
        this.email1 = email1;
        this.email2 = email2;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getAddress4() {
        return address4;
    }

    public void setAddress4(String address4) {
        this.address4 = address4;
    }

    public String getAddress5() {
        return address5;
    }

    public void setAddress5(String address5) {
        this.address5 = address5;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getEmail1() {
        return email1;
    }

    public void setEmail1(String email1) {
        this.email1 = email1;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(String email2) {
        this.email2 = email2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AddressParts that = (AddressParts) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(address1, that.address1) &&
                Objects.equals(email1, that.email1) ;
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, firstName, middleName, lastName, address1, address2, address3, address4, address5, postCode, email1, email2);
    }



    public static AddressPartsBuilder addressParts() {
        return new AddressPartsBuilder();
    }

    public static class AddressPartsBuilder {

        private String name;

        private String  firstName;

        private String  middleName;

        private String lastName;

        private String address1;

        private String address2;

        private String address3;

        private String address4;

        private String address5;

        private String postcode;

        private String email1;

        private String email2;


        public AddressPartsBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public AddressPartsBuilder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public AddressPartsBuilder withMiddleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public AddressPartsBuilder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public AddressPartsBuilder withAddress1(String address1) {
            this.address1 = address1;
            return this;
        }

        public AddressPartsBuilder withAddress2(String address2) {
            this.address2 = address2;
            return this;
        }

        public AddressPartsBuilder withAddress3(String address3) {
            this.address3 = address3;
            return this;
        }

        public AddressPartsBuilder withAddress4(String address4) {
            this.address4 = address4;
            return this;
        }

        public AddressPartsBuilder withAddress5(String address5) {
            this.address5 = address5;
            return this;
        }

        public AddressPartsBuilder withPostCode(String postCode) {
            this.postcode = postCode;
            return this;
        }

        public AddressPartsBuilder withEmail1(String email1) {
            this.email1 = email1;
            return this;
        }

        public AddressPartsBuilder withEmail2(String email2) {
            this.email2 = email2;
            return this;
        }

        public AddressParts build() {
            return new AddressParts(name, firstName, middleName, lastName, address1, address2, address3, address4, address5, postcode,email1,email2);
        }
    }


}
