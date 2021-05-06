package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@SuppressWarnings("squid:S1067")
@Embeddable
public class CpsProsecutor {

    @Column(name = "cps_prosecutor_id")
    private UUID cpsProsecutorId;

    @Column(name = "cps_prosecutor_code")
    private String cpsProsecutorCode;

    public UUID getCpsProsecutorId() {
        return cpsProsecutorId;
    }

    public void setCpsProsecutorId(final UUID cpsProsecutorId) {
        this.cpsProsecutorId = cpsProsecutorId;
    }

    public String getCpsProsecutorCode() {
        return cpsProsecutorCode;
    }

    public void setCpsProsecutorCode(final String cpsProsecutorCode) {
        this.cpsProsecutorCode = cpsProsecutorCode;
    }
}
