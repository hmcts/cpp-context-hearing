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
    private boolean unanimous;
    private UUID enteredHearingId;

    public UUID getTypeId() {
        return typeId;
    }

    public String getVerdictDescription() {
        return verdictDescription;
    }

    public String getVerdictCategory() {
        return verdictCategory;
    }

    public String getNumberOfSplitJurors() {
        return numberOfSplitJurors;
    }

    public LocalDate getVerdictDate() {
        return verdictDate;
    }

    public Integer getNumberOfJurors() {
        return numberOfJurors;
    }

    public boolean isUnanimous() {
        return unanimous;
    }

    public UUID getEnteredHearingId() {
        return enteredHearingId;
    }

    public Verdict setTypeId(UUID typeId) {
        this.typeId = typeId;
        return this;
    }

    public Verdict setVerdictDescription(String verdictDescription) {
        this.verdictDescription = verdictDescription;
        return this;
    }

    public Verdict setVerdictCategory(String verdictCategory) {
        this.verdictCategory = verdictCategory;
        return this;
    }

    public Verdict setNumberOfSplitJurors(String numberOfSplitJurors) {
        this.numberOfSplitJurors = numberOfSplitJurors;
        return this;
    }

    public Verdict setVerdictDate(LocalDate verdictDate) {
        this.verdictDate = verdictDate;
        return this;
    }

    public Verdict setNumberOfJurors(Integer numberOfJurors) {
        this.numberOfJurors = numberOfJurors;
        return this;
    }

    public Verdict setUnanimous(boolean unanimous) {
        this.unanimous = unanimous;
        return this;
    }

    public Verdict setEnteredHearingId(UUID enteredHearingId) {
        this.enteredHearingId = enteredHearingId;
        return this;
    }

    public static Verdict verdict() {
        return new Verdict();
    }
}

