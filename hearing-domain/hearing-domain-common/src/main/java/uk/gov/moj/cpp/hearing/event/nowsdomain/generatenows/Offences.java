package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Offences implements Serializable {

    private UUID id;

    private String code;

    private LocalDate convictionDate;

    private Plea plea;

    private Verdict verdict;

    private String wording;

    private LocalDate startDate;

    private LocalDate endDate;

    public static Offences offences() {
        return new Offences();
    }

    public UUID getId() {
        return this.id;
    }

    public Offences setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getCode() {
        return this.code;
    }

    public Offences setCode(String code) {
        this.code = code;
        return this;
    }

    public LocalDate getConvictionDate() {
        return this.convictionDate;
    }

    public Offences setConvictionDate(LocalDate convictionDate) {
        this.convictionDate = convictionDate;
        return this;
    }

    public Plea getPlea() {
        return this.plea;
    }

    public Offences setPlea(Plea plea) {
        this.plea = plea;
        return this;
    }

    public Verdict getVerdict() {
        return this.verdict;
    }

    public Offences setVerdict(Verdict verdict) {
        this.verdict = verdict;
        return this;
    }

    public String getWording() {
        return this.wording;
    }

    public Offences setWording(String wording) {
        this.wording = wording;
        return this;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public Offences setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public Offences setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }
}
