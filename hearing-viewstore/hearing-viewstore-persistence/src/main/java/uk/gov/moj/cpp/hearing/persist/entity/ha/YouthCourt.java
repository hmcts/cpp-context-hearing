package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.UUID;

@SuppressWarnings("squid:S1067")
@Embeddable
public class YouthCourt {

    @Column(name = "yc_code_id")
    private UUID id;

    @Column(name = "yc_code")
    private Integer courtCode;

    @Column(name = "yc_name")
    private String name;

    @Column(name = "yc_welsh_name")
    private String welshName;


    public YouthCourt() {
        //For JPA
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getCourtCode() {
        return courtCode;
    }

    public void setCourtCode(Integer courtCode) {
        this.courtCode = courtCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWelshName() {
        return welshName;
    }

    public void setWelshName(String welshName) {
        this.welshName = welshName;
    }
}
