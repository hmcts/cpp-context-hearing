package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;

public class LesserOrAlternativeOffence implements Serializable {
    private final static long serialVersionUID = -4619079979222411584L;

    private String offenceTypeId;
    private String code;
    private String convictionDate;
    private String wording;

    public String getOffenceTypeId() {
        return offenceTypeId;
    }

    public void setOffenceTypeId(String offenceTypeId) {
        this.offenceTypeId = offenceTypeId;
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

    public String getWording() {
        return wording;
    }

    public void setWording(String wording) {
        this.wording = wording;
    }
}
