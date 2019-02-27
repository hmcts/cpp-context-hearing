package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class DelegatedPowers {

    @Column(name = "delegated_powers_user_id")
    private UUID delegatedPowersUserId;

    @Column(name = "delegated_powers_first_name")
    private String delegatedPowersFirstName;

    @Column(name = "delegated_powers_last_name")
    private String delegatedPowersLastName;

    public static DelegatedPowers delegatedPowers() {
        return new DelegatedPowers();
    }

    public UUID getDelegatedPowersUserId() {
        return delegatedPowersUserId;
    }

    public DelegatedPowers setDelegatedPowersUserId(UUID delegatedPowersUserId) {
        this.delegatedPowersUserId = delegatedPowersUserId;
        return this;
    }

    public String getDelegatedPowersFirstName() {
        return delegatedPowersFirstName;
    }

    public DelegatedPowers setDelegatedPowersFirstName(String delegatedPowersFirstName) {
        this.delegatedPowersFirstName = delegatedPowersFirstName;
        return this;
    }

    public String getDelegatedPowersLastName() {
        return delegatedPowersLastName;
    }

    public DelegatedPowers setDelegatedPowersLastName(String delegatedPowersLastName) {
        this.delegatedPowersLastName = delegatedPowersLastName;
        return this;
    }
}
