package uk.gov.moj.cpp.hearing.message.shareResults;

import java.time.LocalDate;
import java.util.UUID;

public class LesserOrAlternativeOffence {

    private UUID offenceTypeId;
    private String code;
    private LocalDate convictionDate;
    private String wording;

    public UUID getOffenceTypeId() {
        return offenceTypeId;
    }

    public String getCode() {
        return code;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public String getWording() {
        return wording;
    }

    public LesserOrAlternativeOffence setOffenceTypeId(UUID offenceTypeId) {
        this.offenceTypeId = offenceTypeId;
        return this;
    }

    public LesserOrAlternativeOffence setCode(String code) {
        this.code = code;
        return this;
    }

    public LesserOrAlternativeOffence setConvictionDate(LocalDate convictionDate) {
        this.convictionDate = convictionDate;
        return this;
    }

    public LesserOrAlternativeOffence setWording(String wording) {
        this.wording = wording;
        return this;
    }

    public static LesserOrAlternativeOffence lesserOrAlternativeOffence() {
        return new LesserOrAlternativeOffence();
    }
}
