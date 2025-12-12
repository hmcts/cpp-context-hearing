package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CustodialEstablishment {

    @Column(name = "custodial_establishment_custody")
    private String custody;

    @Column(name = "custodial_establishment_id")
    private UUID id;

    @Column(name = "custodial_establishment_name")
    private String name;


    public CustodialEstablishment() {
        //For JPA
    }

    public String getCustody() {
        return custody;
    }

    public CustodialEstablishment setCustody(String custody) {
        this.custody = custody;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public CustodialEstablishment setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public CustodialEstablishment setName(String name) {
        this.name = name;
        return this;
    }

}
