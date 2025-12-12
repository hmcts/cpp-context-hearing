package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;

public class Verdict implements Serializable {

    private final static long serialVersionUID = -8851463506915110861L;
    private String typeId;
    private String verdictDescription;
    private String verdictCategory;
    private LesserOrAlternativeOffence lesserOrAlternativeOffence;
    private String numberOfSplitJurors;
    private String verdictDate;
    private Integer numberOfJurors;
    private Boolean unanimous;
    private String enteredHearingId;

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getVerdictDescription() {
        return verdictDescription;
    }

    public void setVerdictDescription(String verdictDescription) {
        this.verdictDescription = verdictDescription;
    }

    public String getVerdictCategory() {
        return verdictCategory;
    }

    public void setVerdictCategory(String verdictCategory) {
        this.verdictCategory = verdictCategory;
    }

    public LesserOrAlternativeOffence getLesserOrAlternativeOffence() {
        return lesserOrAlternativeOffence;
    }

    public void setLesserOrAlternativeOffence(LesserOrAlternativeOffence lesserOrAlternativeOffence) {
        this.lesserOrAlternativeOffence = lesserOrAlternativeOffence;
    }

    public String getNumberOfSplitJurors() {
        return numberOfSplitJurors;
    }

    public void setNumberOfSplitJurors(String numberOfSplitJurors) {
        this.numberOfSplitJurors = numberOfSplitJurors;
    }

    public String getVerdictDate() {
        return verdictDate;
    }

    public void setVerdictDate(String verdictDate) {
        this.verdictDate = verdictDate;
    }

    public Integer getNumberOfJurors() {
        return numberOfJurors;
    }

    public void setNumberOfJurors(Integer numberOfJurors) {
        this.numberOfJurors = numberOfJurors;
    }

    public Boolean getUnanimous() {
        return unanimous;
    }

    public void setUnanimous(Boolean unanimous) {
        this.unanimous = unanimous;
    }

    public String getEnteredHearingId() {
        return enteredHearingId;
    }

    public void setEnteredHearingId(String enteredHearingId) {
        this.enteredHearingId = enteredHearingId;
    }
}
