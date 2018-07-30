package uk.gov.moj.cpp.hearing.event.nows.order;

import java.io.Serializable;

public class OrderDefendant implements Serializable {
    private final static long serialVersionUID = 8453916860863213534L;
    private String name;
    private String dateOfBirth;
    private Address address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public static Builder builder() {
        return new Builder();
    }
    public static final class Builder {
        private String name;
        private String dateOfBirth;

        private Address address;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder withAddress(Address address) {
            this.address = address;
            return this;
        }

        public OrderDefendant build() {
            OrderDefendant defendant = new OrderDefendant();
            defendant.setName(name);
            defendant.setDateOfBirth(dateOfBirth);
            defendant.setAddress(address);
            return defendant;
        }
    }
}
