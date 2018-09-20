package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Embeddable;
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
}