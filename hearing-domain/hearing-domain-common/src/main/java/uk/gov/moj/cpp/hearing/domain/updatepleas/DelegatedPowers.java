package uk.gov.moj.cpp.hearing.domain.updatepleas;

import java.io.Serializable;
import java.util.UUID;

public class DelegatedPowers implements Serializable {

    private UUID userId;

    private String firstName;

    private String lastName;

    public UUID getUserId() {
        return this.userId;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public DelegatedPowers setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public DelegatedPowers setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public DelegatedPowers setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public static DelegatedPowers delegatedPowers() {
        return new DelegatedPowers();
    }
}
