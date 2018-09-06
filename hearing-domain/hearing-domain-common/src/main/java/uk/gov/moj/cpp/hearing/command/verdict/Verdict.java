package uk.gov.moj.cpp.hearing.command.verdict;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Verdict implements Serializable {

    private static final long serialVersionUID = 1L;
    private UUID offenceId;
    private VerdictType verdictType;
    private LocalDate verdictDate;
    private LesserOffence lesserOffence;
    private Jurors jurors;

    public Verdict() {
    }

    @JsonCreator
    protected Verdict(
            @JsonProperty("offenceId") final UUID offenceId,
            @JsonProperty("verdictType") final VerdictType verdictType,
            @JsonProperty("verdictDate") final LocalDate verdictDate,
            @JsonProperty("lesserOffence") final LesserOffence lesserOffence,
            @JsonProperty("jurors") final Jurors jurors) {
        this.offenceId = offenceId;
        this.verdictType = verdictType;
        this.verdictDate = verdictDate;
        this.lesserOffence = lesserOffence;
        this.jurors = jurors;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public VerdictType getVerdictType() {
        return verdictType;
    }

    public LocalDate getVerdictDate() {
        return verdictDate;
    }

    public LesserOffence getLesserOffence() {
        return lesserOffence;
    }

    public Jurors getJurors() {
        return jurors;
    }

    public Verdict setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public Verdict setVerdictType(VerdictType verdictType) {
        this.verdictType = verdictType;
        return this;
    }

    public Verdict setVerdictDate(LocalDate verdictDate) {
        this.verdictDate = verdictDate;
        return this;
    }

    public Verdict setLesserOffence(LesserOffence lesserOffence) {
        this.lesserOffence = lesserOffence;
        return this;
    }

    public Verdict setJurors(Jurors jurors) {
        this.jurors = jurors;
        return this;
    }


    public static Verdict verdict(){
        return new Verdict();
    }

}