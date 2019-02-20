package uk.gov.moj.cpp.hearing.event.nows.order;

import java.io.Serializable;


public class Address implements Serializable {

    private final static long serialVersionUID = -468345944222529099L;
    private String line1;
    private String line2;
    private String postCode;

    public static Builder builder() {
        return new Builder();
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public static final class Builder {
        private String line1;
        private String line2;
        private String postCode;

        private Builder() {
        }

        public Builder withLine1(String line1) {
            this.line1 = line1;
            return this;
        }

        public Builder withLine2(String line2) {
            this.line2 = line2;
            return this;
        }

        public Builder withPostCode(String postCode) {
            this.postCode = postCode;
            return this;
        }

        public Address build() {
            Address address = new Address();
            address.setLine1(line1);
            address.setLine2(line2);
            address.setPostCode(postCode);
            return address;
        }
    }
}
