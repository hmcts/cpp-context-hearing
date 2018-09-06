package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DelegatedPowers {

    @Column(name = "delegated_powers_user_id")
    private UUID delegatedPowersUserId;

    @Column(name = "delegated_powers_first_name")
    private String delegatedPowersFirstName;

    @Column(name = "delegated_powers_last_name")
    private String delegatedPowersLastName;

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

    public static DelegatedPowers delegatedPowers() {
        return new DelegatedPowers();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DelegatedPowers that = (DelegatedPowers) o;
        return Objects.equals(delegatedPowersUserId, that.delegatedPowersUserId) &&
                Objects.equals(delegatedPowersFirstName, that.delegatedPowersFirstName) &&
                Objects.equals(delegatedPowersLastName, that.delegatedPowersLastName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(delegatedPowersUserId, delegatedPowersFirstName, delegatedPowersLastName);
    }
}
