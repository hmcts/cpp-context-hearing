package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class Cases {

    private java.util.UUID id;

    private String urn;

    private String bailStatus;

    private String custodyTimeLimitDate;

    private java.util.List<Offences> offences;

    public static Cases cases() {
        return new Cases();
    }

    public java.util.UUID getId() {
        return this.id;
    }

    public Cases setId(java.util.UUID id) {
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

    public String getCustodyTimeLimitDate() {
        return this.custodyTimeLimitDate;
    }

    public Cases setCustodyTimeLimitDate(String custodyTimeLimitDate) {
        this.custodyTimeLimitDate = custodyTimeLimitDate;
        return this;
    }

    public java.util.List<Offences> getOffences() {
        return this.offences;
    }

    public Cases setOffences(java.util.List<Offences> offences) {
        this.offences = offences;
        return this;
    }
}
