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


    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public Plea getPlea() {
        return plea;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public String getWording() {
        return wording;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Offence setId(UUID id) {
        this.id = id;
        return this;
    }

    public Offence setCode(String code) {
        this.code = code;
        return this;
    }

    public Offence setConvictionDate(LocalDate convictionDate) {
        this.convictionDate = convictionDate;
        return this;
    }

    public Offence setPlea(Plea plea) {
        this.plea = plea;
        return this;
    }

    public Offence setVerdict(Verdict verdict) {
        this.verdict = verdict;
        return this;
    }

    public Offence setWording(String wording) {
        this.wording = wording;
        return this;
    }

    public Offence setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public Offence setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public static Offence offence() {
        return new Offence();
    }
}
