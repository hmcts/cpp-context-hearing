package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("squid:S1067")
@Embeddable
public class Organisation {

    private UUID id;

    private String name;

    private String incorporationNumber;

    private String registeredCharityNumber;

    private Address address;

    private Contact contact;

    public Organisation() {
        //For JPA
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIncorporationNumber() {
        return incorporationNumber;
    }

    public String getRegisteredCharityNumber() {
        return registeredCharityNumber;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIncorporationNumber(String incorporationNumber) {
        this.incorporationNumber = incorporationNumber;
    }

    public void setRegisteredCharityNumber(String registeredCharityNumber) {
        this.registeredCharityNumber = registeredCharityNumber;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.incorporationNumber, this.registeredCharityNumber);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        final Organisation a = (Organisation) o;
        return Objects.equals(this.name, a.name)
                && Objects.equals(this.incorporationNumber, a.incorporationNumber)
                && Objects.equals(this.registeredCharityNumber, a.registeredCharityNumber);
    }
}