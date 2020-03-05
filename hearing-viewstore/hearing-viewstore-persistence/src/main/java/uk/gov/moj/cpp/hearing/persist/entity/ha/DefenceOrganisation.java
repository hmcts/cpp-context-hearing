package uk.gov.moj.cpp.hearing.persist.entity.ha;


import javax.persistence.Column;
import javax.persistence.Embeddable;

@SuppressWarnings("squid:S1186")
@Embeddable
public class DefenceOrganisation {
    @Column(name = "laa_contract_ref", unique = true)
    private String laaContractNumber;

    private String name;

    private String incorporationNumber;

    private String registeredCharityNumber;

    private Address address;

    private Contact contact;


    public DefenceOrganisation() {
    }

    public String getLaaContractNumber() {
        return laaContractNumber;
    }

    public void setLaaContractNumber(final String laaContractNumber) {
        this.laaContractNumber = laaContractNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getIncorporationNumber() {
        return incorporationNumber;
    }

    public void setIncorporationNumber(final String incorporationNumber) {
        this.incorporationNumber = incorporationNumber;
    }

    public String getRegisteredCharityNumber() {
        return registeredCharityNumber;
    }

    public void setRegisteredCharityNumber(final String registeredCharityNumber) {
        this.registeredCharityNumber = registeredCharityNumber;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(final Contact contact) {
        this.contact = contact;
    }
}
