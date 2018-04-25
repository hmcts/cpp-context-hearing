package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;

public class Offence implements Serializable {

    private final static long serialVersionUID = -4649347396552242435L;
    private String id;
    private String code;
    private String convictionDate;
    private Plea plea;
    private Verdict verdict;
    private String wording;
    private String startDate;
    private String endDate;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getConvictionDate() {
        return convictionDate;
    }

    public void setConvictionDate(String convictionDate) {
        this.convictionDate = convictionDate;
    }

    public Plea getPlea() {
        return plea;
    }

    public void setPlea(Plea plea) {
        this.plea = plea;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public void setVerdict(Verdict verdict) {
        this.verdict = verdict;
    }

    public String getWording() {
        return wording;
    }

    public void setWording(String wording) {
        this.wording = wording;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
