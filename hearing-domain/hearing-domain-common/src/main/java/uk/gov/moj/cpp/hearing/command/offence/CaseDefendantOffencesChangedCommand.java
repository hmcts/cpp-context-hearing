package uk.gov.moj.cpp.hearing.command.offence;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CaseDefendantOffencesChangedCommand {

    private LocalDate modifiedDate;

    private List<DefendantOffences> updatedOffences;

    private List<DeletedOffences> deletedOffences;

    private List<DefendantOffences> addedOffences;

    public CaseDefendantOffencesChangedCommand() {
    }

    @JsonCreator
    private CaseDefendantOffencesChangedCommand(@JsonProperty("modifiedDate") final LocalDate modifiedDate,
                                                @JsonProperty("addedOffences") final List<DefendantOffences> addedOffences,
                                                @JsonProperty("updatedOffences") final List<DefendantOffences> updatedOffences,
                                                @JsonProperty("deletedOffences") final List<DeletedOffences> deletedOffences) {
        this.modifiedDate = modifiedDate;

        this.updatedOffences = nonNull(updatedOffences) ? new ArrayList<>(updatedOffences) : new ArrayList<>();

        this.deletedOffences = nonNull(deletedOffences) ? new ArrayList<>(deletedOffences) : new ArrayList<>();

        this.addedOffences = nonNull(addedOffences) ? new ArrayList<>(addedOffences) : new ArrayList<>();
    }

    public LocalDate getModifiedDate() {
        return modifiedDate;
    }

    public List<DefendantOffences> getUpdatedOffences() {
        return updatedOffences;
    }

    public List<DeletedOffences> getDeletedOffences() {
        return deletedOffences;
    }

    public List<DefendantOffences> getAddedOffences() {
        return addedOffences;
    }

    public CaseDefendantOffencesChangedCommand setModifiedDate(LocalDate modifiedDate) {
        this.modifiedDate = modifiedDate;
        return this;
    }

    public CaseDefendantOffencesChangedCommand setUpdatedOffences(List<DefendantOffences> updatedOffences) {
        this.updatedOffences = new ArrayList<>(updatedOffences);
        return this;
    }

    public CaseDefendantOffencesChangedCommand setDeletedOffences(List<DeletedOffences> deletedOffences) {
        this.deletedOffences = new ArrayList<>(deletedOffences);
        return this;
    }

    public CaseDefendantOffencesChangedCommand setAddedOffences(List<DefendantOffences> addedOffences) {
        this.addedOffences = new ArrayList<>(addedOffences);
        return this;
    }

    public static CaseDefendantOffencesChangedCommand caseDefendantOffencesChangedCommand() {
        return new CaseDefendantOffencesChangedCommand();
    }
}