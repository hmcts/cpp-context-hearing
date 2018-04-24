package uk.gov.moj.cpp.hearing.message.shareResults;

import java.time.LocalDate;
import java.util.UUID;

public class Hearing {

    private UUID id;
    private String hearingType;
    private String courtCentreName;
    private String courtCode;
    private LocalDate startDate;
    private String judgeName;
    private String prosecutorName;
    private String defenceName;


    public UUID getId() {
        return id;
    }

    public String getHearingType() {
        return hearingType;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public String getCourtCode() {
        return courtCode;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public String getJudgeName() {
        return judgeName;
    }

    public String getProsecutorName() {
        return prosecutorName;
    }

    public String getDefenceName() {
        return defenceName;
    }

    public Hearing setId(UUID id) {
        this.id = id;
        return this;
    }

    public Hearing setHearingType(String hearingType) {
        this.hearingType = hearingType;
        return this;
    }

    public Hearing setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
        return this;
    }

    public Hearing setCourtCode(String courtCode) {
        this.courtCode = courtCode;
        return this;
    }

    public Hearing setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public Hearing setJudgeName(String judgeName) {
        this.judgeName = judgeName;
        return this;
    }

    public Hearing setProsecutorName(String prosecutorName) {
        this.prosecutorName = prosecutorName;
        return this;
    }

    public Hearing setDefenceName(String defenceName) {
        this.defenceName = defenceName;
        return this;
    }

    public static Hearing hearing(){
        return new Hearing();
    }
}
