package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class Verdict {

    private String typeId;

    private String verdictDescription;

    private String verdictCategory;

    private LesserOrAlternativeOffence lesserOrAlternativeOffence;

    private String numberOfSplitJurors;

    private String verdictDate;

    private Integer numberOfJurors;

    private Boolean unanimous;

    private java.util.UUID enteredHearingId;

    public static Verdict verdict() {
        return new Verdict();
    }

    public String getTypeId() {
        return this.typeId;
    }

    public Verdict setTypeId(String typeId) {
        this.typeId = typeId;
        return this;
    }

    public String getVerdictDescription() {
        return this.verdictDescription;
    }

    public Verdict setVerdictDescription(String verdictDescription) {
        this.verdictDescription = verdictDescription;
        return this;
    }

    public String getVerdictCategory() {
        return this.verdictCategory;
    }

    public Verdict setVerdictCategory(String verdictCategory) {
        this.verdictCategory = verdictCategory;
        return this;
    }

    public LesserOrAlternativeOffence getLesserOrAlternativeOffence() {
        return this.lesserOrAlternativeOffence;
    }

    public Verdict setLesserOrAlternativeOffence(LesserOrAlternativeOffence lesserOrAlternativeOffence) {
        this.lesserOrAlternativeOffence = lesserOrAlternativeOffence;
        return this;
    }

    public String getNumberOfSplitJurors() {
        return this.numberOfSplitJurors;
    }

    public Verdict setNumberOfSplitJurors(String numberOfSplitJurors) {
        this.numberOfSplitJurors = numberOfSplitJurors;
        return this;
    }

    public String getVerdictDate() {
        return this.verdictDate;
    }

    public Verdict setVerdictDate(String verdictDate) {
        this.verdictDate = verdictDate;
        return this;
    }

    public Integer getNumberOfJurors() {
        return this.numberOfJurors;
    }

    public Verdict setNumberOfJurors(Integer numberOfJurors) {
        this.numberOfJurors = numberOfJurors;
        return this;
    }

    public Boolean getUnanimous() {
        return this.unanimous;
    }

    public Verdict setUnanimous(Boolean unanimous) {
        this.unanimous = unanimous;
        return this;
    }

    public java.util.UUID getEnteredHearingId() {
        return this.enteredHearingId;
    }

    public Verdict setEnteredHearingId(java.util.UUID enteredHearingId) {
        this.enteredHearingId = enteredHearingId;
        return this;
    }
}
