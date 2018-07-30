package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class Cases implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private String urn;

    private String bailStatus;

    private LocalDate custodyTimeLimitDate;

    private List<Offences> offences;

    public static Cases cases() {
        return new Cases();
    }

    public UUID getId() {
        return this.id;
    }

    public Cases setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getUrn() {
        return this.urn;
    }

    public Cases setUrn(String urn) {
        this.urn = urn;
        return this;
    }

    public String getBailStatus() {
        return this.bailStatus;
    }

    public Cases setBailStatus(String bailStatus) {
        this.bailStatus = bailStatus;
        return this;
    }

    public LocalDate getCustodyTimeLimitDate() {
        return this.custodyTimeLimitDate;
    }

    public Cases setCustodyTimeLimitDate(LocalDate custodyTimeLimitDate) {
        this.custodyTimeLimitDate = custodyTimeLimitDate;
        return this;
    }

    public List<Offences> getOffences() {
        return this.offences;
    }

    public Cases setOffences(List<Offences> offences) {
        this.offences = offences;
        return this;
    }
}
