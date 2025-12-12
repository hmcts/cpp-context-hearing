package uk.gov.moj.cpp.hearing.message.shareResults;

import java.time.LocalDate;
import java.util.UUID;

public class Verdict {

    private UUID typeId;
    private String verdictDescription;
    private String verdictCategory;
    private String numberOfSplitJurors;
    private LocalDate verdictDate;
    private Integer numberOfJurors;
    private Boolean unanimous;
    private UUID enteredHearingId;

    public static Verdict verdict() {
        return new Verdict();
    }

    public UUID getTypeId() {
        return typeId;
    }

    public Verdict setTypeId(UUID typeId) {
        this.typeId = typeId;
        return this;
    }

    public String getVerdictDescription() {
        return verdictDescription;
    }

    public Verdict setVerdictDescription(String verdictDescription) {
        this.verdictDescription = verdictDescription;
        return this;
    }

    public String getVerdictCategory() {
        return verdictCategory;
    }

    public Verdict setVerdictCategory(String verdictCategory) {
        this.verdictCategory = verdictCategory;
        return this;
    }

    public String getNumberOfSplitJurors() {
        return numberOfSplitJurors;
    }

    public Verdict setNumberOfSplitJurors(String numberOfSplitJurors) {
        this.numberOfSplitJurors = numberOfSplitJurors;
        return this;
    }

    public LocalDate getVerdictDate() {
        return verdictDate;
    }

    public Verdict setVerdictDate(LocalDate verdictDate) {
        this.verdictDate = verdictDate;
        return this;
    }

    public Integer getNumberOfJurors() {
        return numberOfJurors;
    }

    public Verdict setNumberOfJurors(Integer numberOfJurors) {
        this.numberOfJurors = numberOfJurors;
        return this;
    }

    public Boolean getUnanimous() {
        return unanimous;
    }

    public Verdict setUnanimous(Boolean unanimous) {
        this.unanimous = unanimous;
        return this;
    }

    public UUID getEnteredHearingId() {
        return enteredHearingId;
    }

    public Verdict setEnteredHearingId(UUID enteredHearingId) {
        this.enteredHearingId = enteredHearingId;
        return this;
    }
}

