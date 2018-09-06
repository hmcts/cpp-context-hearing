package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@SuppressWarnings("squid:S1067")
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

    @Column(name = "address_5")
    private String address5;

    @Column(name = "post_code")
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

    public String getAddress5() {
        return address5;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public void setAddress4(String address4) {
        this.address4 = address4;
    }

    public void setAddress5(String address5) {
        this.address5 = address5;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.address1, this.address2, this.address3, this.address4, this.address5, this.postCode);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        final Address a = (Address) o;
        return Objects.equals(this.address1, a.address1)
                && Objects.equals(this.address2, a.address2)
                && Objects.equals(this.address3, a.address3)
                && Objects.equals(this.address4, a.address4)
                && Objects.equals(this.address5, a.address5)
                && Objects.equals(this.postCode, a.postCode);
    }
}