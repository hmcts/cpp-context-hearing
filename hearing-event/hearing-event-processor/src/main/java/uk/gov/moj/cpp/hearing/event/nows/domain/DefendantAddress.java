
package uk.gov.moj.cpp.hearing.event.nows.domain;

import java.io.Serializable;


public class DefendantAddress implements Serializable
{

    private final static long serialVersionUID = -468345944222529099L;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String postCode;

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

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String address1;
        private String address2;
        private String address3;
        private String address4;
        private String postCode;

        private Builder() {
        }



        public Builder withAddress1(String address1) {
            this.address1 = address1;
            return this;
        }

        public Builder withAddress2(String address2) {
            this.address2 = address2;
            return this;
        }

        public Builder withAddress3(String address3) {
            this.address3 = address3;
            return this;
        }

        public Builder withAddress4(String address4) {
            this.address4 = address4;
            return this;
        }

        public Builder withPostCode(String postCode) {
            this.postCode = postCode;
            return this;
        }

        public DefendantAddress build() {
            DefendantAddress defendantAddress = new DefendantAddress();
            defendantAddress.setAddress1(address1);
            defendantAddress.setAddress2(address2);
            defendantAddress.setAddress3(address3);
            defendantAddress.setAddress4(address4);
            defendantAddress.setPostCode(postCode);
            return defendantAddress;
        }
    }
}
