package uk.gov.moj.cpp.hearing.message.shareResults;

import java.time.LocalDate;
import java.util.UUID;

public class Offence {

    private UUID id;
    private String code;
    private LocalDate convictionDate;
    private Plea plea;
    private Verdict verdict;
    private String wording;
    private LocalDate startDate;
    private LocalDate endDate;

    public static Offence offence() {
        return new Offence();
    }

    public UUID getId() {
        return id;
    }

    public Offence setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getCode() {
        return code;
    }

    public Offence setCode(String code) {
        this.code = code;
        return this;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public Offence setConvictionDate(LocalDate convictionDate) {
        this.convictionDate = convictionDate;
        return this;
    }

    public Plea getPlea() {
        return plea;
    }

    public Offence setPlea(Plea plea) {
        this.plea = plea;
        return this;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public Offence setVerdict(Verdict verdict) {
        this.verdict = verdict;
        return this;
    }

    public String getWording() {
        return wording;
    }

    public Offence setWording(String wording) {
        this.wording = wording;
        return this;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public Offence setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Offence setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }
}
