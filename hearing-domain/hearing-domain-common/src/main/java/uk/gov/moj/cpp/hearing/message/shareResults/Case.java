package uk.gov.moj.cpp.hearing.message.shareResults;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class Case {
    private UUID id;
    private String urn;
    private String bailStatus;
    private LocalDate custodyTimeLimitDate;
    private List<Offence> offences;

    public static Case legalCase() {
        return new Case();
    }

    public UUID getId() {
        return id;
    }

    public Case setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getUrn() {
        return urn;
    }

    public Case setUrn(String urn) {
        this.urn = urn;
        return this;
    }

    public String getBailStatus() {
        return bailStatus;
    }

    public Case setBailStatus(String bailStatus) {
        this.bailStatus = bailStatus;
        return this;
    }

    public LocalDate getCustodyTimeLimitDate() {
        return custodyTimeLimitDate;
    }

    public Case setCustodyTimeLimitDate(LocalDate custodyTimeLimitDate) {
        this.custodyTimeLimitDate = custodyTimeLimitDate;
        return this;
    }

    public List<Offence> getOffences() {
        return offences;
    }

    public Case setOffences(List<Offence> offences) {
        this.offences = offences;
        return this;
    }
}
