package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class LesserOrAlternativeOffence {

    private String offenceTypeId;

    private String code;

    private String convictionDate;

    private String wording;

    public static LesserOrAlternativeOffence lesserOrAlternativeOffence() {
        return new LesserOrAlternativeOffence();
    }

    public String getOffenceTypeId() {
        return this.offenceTypeId;
    }

    public LesserOrAlternativeOffence setOffenceTypeId(String offenceTypeId) {
        this.offenceTypeId = offenceTypeId;
        return this;
    }

    public String getCode() {
        return this.code;
    }

    public LesserOrAlternativeOffence setCode(String code) {
        this.code = code;
        return this;
    }

    public String getConvictionDate() {
        return this.convictionDate;
    }

    public LesserOrAlternativeOffence setConvictionDate(String convictionDate) {
        this.convictionDate = convictionDate;
        return this;
    }

    public String getWording() {
        return this.wording;
    }

    public LesserOrAlternativeOffence setWording(String wording) {
        this.wording = wording;
        return this;
    }
}
