package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.util.UUID;

public class Offences implements Serializable {

    private UUID id;

    private String code;

    private String convictionDate;

    private Plea plea;

    private Verdict verdict;

    private String wording;

    private String startDate;

    private String endDate;

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

    public String getConvictionDate() {
        return this.convictionDate;
    }

    public Offences setConvictionDate(String convictionDate) {
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

    public String getStartDate() {
        return this.startDate;
    }

    public Offences setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public String getEndDate() {
        return this.endDate;
    }

    public Offences setEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }
}
