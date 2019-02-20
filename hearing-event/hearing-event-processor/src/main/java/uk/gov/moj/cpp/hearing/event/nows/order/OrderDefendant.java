package uk.gov.moj.cpp.hearing.event.nows.order;

import java.io.Serializable;
import java.time.LocalDate;

public class OrderDefendant implements Serializable {
    private final static long serialVersionUID = 2L;

    private String name;
    private LocalDate dateOfBirth;
    private Address address;

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public static final class Builder {
        private String name;
        private LocalDate dateOfBirth;

        private Address address;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDateOfBirth(LocalDate dateOfBirth) {
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
