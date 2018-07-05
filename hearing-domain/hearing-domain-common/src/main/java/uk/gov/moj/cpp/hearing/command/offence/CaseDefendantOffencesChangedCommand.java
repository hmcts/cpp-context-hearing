package uk.gov.moj.cpp.hearing.command.offence;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CaseDefendantOffencesChangedCommand {

    private final LocalDate modifiedDate;

    private final List<UpdatedOffence> updatedOffences;

    private final List<DeletedOffence> deletedOffences;

    private final List<AddedOffence> addedOffences;

    @JsonCreator
    private CaseDefendantOffencesChangedCommand(@JsonProperty("modifiedDate") final LocalDate modifiedDate,
                                                @JsonProperty("addedOffences") final List<AddedOffence> addedOffences,
                                                @JsonProperty("updatedOffences") final List<UpdatedOffence> updatedOffences,
                                                @JsonProperty("deletedOffences") final List<DeletedOffence> deletedOffences) {
        this.modifiedDate = modifiedDate;

        this.updatedOffences = nonNull(updatedOffences) ? new ArrayList<>(updatedOffences) : new ArrayList<>();

        this.deletedOffences = nonNull(deletedOffences) ? new ArrayList<>(deletedOffences) : new ArrayList<>();

        this.addedOffences = nonNull(addedOffences) ? new ArrayList<>(addedOffences) : new ArrayList<>();
    }

    public LocalDate getModifiedDate() {
        return modifiedDate;
    }

    public List<UpdatedOffence> getUpdatedOffences() {
        return updatedOffences;
    }

    public List<DeletedOffence> getDeletedOffences() {
        return deletedOffences;
    }

    public List<AddedOffence> getAddedOffences() {
        return addedOffences;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private LocalDate modifiedDate;

        private List<UpdatedOffence> updatedOffences;

        private List<DeletedOffence> deletedOffences;

        private List<AddedOffence> addedOffences;

        public Builder withModifiedDate(final LocalDate modifiedDate) {
            this.modifiedDate = modifiedDate;
            return this;
        }

        public Builder withUpdateOffences(final List<UpdatedOffence> updatedOffences) {
            this.updatedOffences = updatedOffences;
            return this;
        }

        public Builder withDeletedOffences(final List<DeletedOffence> deletedOffences) {
            this.deletedOffences = deletedOffences;
            return this;
        }

        public Builder withAddedOffences(final List<AddedOffence> addedOffences) {
            this.addedOffences = addedOffences;
            return this;
        }

        public CaseDefendantOffencesChangedCommand build() {
            return new CaseDefendantOffencesChangedCommand(modifiedDate,
                    ofNullable(addedOffences).orElse(new ArrayList<>()),
                    ofNullable(updatedOffences).orElse(new ArrayList<>()),
                    ofNullable(deletedOffences).orElse(new ArrayList<>()));
        }
    }
}