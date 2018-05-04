package uk.gov.moj.cpp.hearing.event.nows.domain;

import java.io.Serializable;

public class NowsOrderDefendant implements Serializable {
    private final static long serialVersionUID = 8453916860863213534L;
    private String name;
    private String dateOfBirth;
    private DefendantAddress address;

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

    public DefendantAddress getAddress() {
        return address;
    }

    public void setAddress(DefendantAddress address) {
        this.address = address;
    }

    public static Builder builder() {
        return new Builder();
    }
    public static final class Builder {
        private String name;
        private String dateOfBirth;

        private DefendantAddress address;

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

        public Builder withAddress(DefendantAddress address) {
            this.address = address;
            return this;
        }

        public NowsOrderDefendant build() {
            NowsOrderDefendant nowsOrderDefendant = new NowsOrderDefendant();
            nowsOrderDefendant.setName(name);
            nowsOrderDefendant.setDateOfBirth(dateOfBirth);
            nowsOrderDefendant.setAddress(address);
            return nowsOrderDefendant;
        }
    }
}
