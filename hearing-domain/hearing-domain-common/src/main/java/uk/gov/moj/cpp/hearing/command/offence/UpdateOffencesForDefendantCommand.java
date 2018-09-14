package uk.gov.moj.cpp.hearing.command.offence;

import static java.util.Optional.ofNullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateOffencesForDefendantCommand {

    private LocalDate modifiedDate;
    private List<DefendantCaseOffences> updatedOffences;
    private List<DeletedOffences> deletedOffences;
    private List<DefendantCaseOffences> addedOffences;

    public UpdateOffencesForDefendantCommand() {
    }

    @JsonCreator
    private UpdateOffencesForDefendantCommand(@JsonProperty(value = "modifiedDate", required = true) final LocalDate modifiedDate,
            @JsonProperty(value = "addedOffences", required = false) final List<DefendantCaseOffences> addedOffences,
            @JsonProperty(value ="updatedOffences", required = false) final List<DefendantCaseOffences> updatedOffences,
            @JsonProperty(value ="deletedOffences", required = false) final List<DeletedOffences> deletedOffences) {
        this.modifiedDate = modifiedDate;
        this.updatedOffences = ofNullable(updatedOffences).orElse(new ArrayList<>());
        this.deletedOffences = ofNullable(deletedOffences).orElse(new ArrayList<>());
        this.addedOffences = ofNullable(addedOffences).orElse(new ArrayList<>());
    }

    public LocalDate getModifiedDate() {
        return modifiedDate;
    }

    public List<DefendantCaseOffences> getUpdatedOffences() {
        return updatedOffences;
    }

    public List<DeletedOffences> getDeletedOffences() {
        return deletedOffences;
    }

    public List<DefendantCaseOffences> getAddedOffences() {
        return addedOffences;
    }

    public UpdateOffencesForDefendantCommand setModifiedDate(LocalDate modifiedDate) {
        this.modifiedDate = modifiedDate;
        return this;
    }

    public UpdateOffencesForDefendantCommand setUpdatedOffences(List<DefendantCaseOffences> updatedOffences) {
        this.updatedOffences = new ArrayList<>(updatedOffences);
        return this;
    }

    public UpdateOffencesForDefendantCommand setDeletedOffences(List<DeletedOffences> deletedOffences) {
        this.deletedOffences = new ArrayList<>(deletedOffences);
        return this;
    }

    public UpdateOffencesForDefendantCommand setAddedOffences(List<DefendantCaseOffences> addedOffences) {
        this.addedOffences = new ArrayList<>(addedOffences);
        return this;
    }

    public static UpdateOffencesForDefendantCommand updateOffencesForDefendantCommand() {
        return new UpdateOffencesForDefendantCommand();
    }
}