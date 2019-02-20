package uk.gov.moj.cpp.hearing.message.shareResults;

import java.time.LocalDate;
import java.util.UUID;

public class LesserOrAlternativeOffence {

    private UUID offenceTypeId;
    private String code;
    private LocalDate convictionDate;
    private String wording;

    public static LesserOrAlternativeOffence lesserOrAlternativeOffence() {
        return new LesserOrAlternativeOffence();
    }

    public UUID getOffenceTypeId() {
        return offenceTypeId;
    }

    public LesserOrAlternativeOffence setOffenceTypeId(UUID offenceTypeId) {
        this.offenceTypeId = offenceTypeId;
        return this;
    }

    public String getCode() {
        return code;
    }

    public LesserOrAlternativeOffence setCode(String code) {
        this.code = code;
        return this;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public LesserOrAlternativeOffence setConvictionDate(LocalDate convictionDate) {
        this.convictionDate = convictionDate;
        return this;
    }

    public String getWording() {
        return wording;
    }

    public LesserOrAlternativeOffence setWording(String wording) {
        this.wording = wording;
        return this;
    }
}
