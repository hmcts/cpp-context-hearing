package uk.gov.moj.cpp.hearing.common;

import java.util.Objects;

public class AddressReusableInfo {

    private String parentguardiansaddressAddress1;
    private String parentguardiansaddressPostCode;
    private String parentguardiansaddressEmailAddress1;
    private Integer phoneNumber;

    public String getParentguardiansaddressAddress1() {
        return parentguardiansaddressAddress1;
    }

    public void setParentguardiansaddressAddress1(final String parentguardiansaddressAddress1) {
        this.parentguardiansaddressAddress1 = parentguardiansaddressAddress1;
    }

    public String getParentguardiansaddressPostCode() {
        return parentguardiansaddressPostCode;
    }

    public void setParentguardiansaddressPostCode(final String parentguardiansaddressPostCode) {
        this.parentguardiansaddressPostCode = parentguardiansaddressPostCode;
    }

    public String getParentguardiansaddressEmailAddress1() {
        return parentguardiansaddressEmailAddress1;
    }

    public void setParentguardiansaddressEmailAddress1(final String parentguardiansaddressEmailAddress1) {
        this.parentguardiansaddressEmailAddress1 = parentguardiansaddressEmailAddress1;
    }

    public Integer getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final Integer phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AddressReusableInfo that = (AddressReusableInfo) o;
        return Objects.equals(parentguardiansaddressAddress1, that.parentguardiansaddressAddress1) &&
                Objects.equals(parentguardiansaddressPostCode, that.parentguardiansaddressPostCode) &&
                Objects.equals(parentguardiansaddressEmailAddress1, that.parentguardiansaddressEmailAddress1) &&
                Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentguardiansaddressAddress1, parentguardiansaddressPostCode, parentguardiansaddressEmailAddress1, phoneNumber);
    }
}
