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

    private final List<DefendantOffences> updatedOffences;

    private final List<DeletedOffences> deletedOffences;

    private final List<DefendantOffences> addedOffences;

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private LocalDate modifiedDate;

        private List<DefendantOffences> updatedOffences;

        private List<DeletedOffences> deletedOffences;

        private List<DefendantOffences> addedOffences;

        public Builder withModifiedDate(final LocalDate modifiedDate) {
            this.modifiedDate = modifiedDate;
            return this;
        }

        public Builder withUpdateOffences(final List<DefendantOffences> updatedOffences) {
            this.updatedOffences = updatedOffences;
            return this;
        }

        public Builder withDeletedOffences(final List<DeletedOffences> deletedOffences) {
            this.deletedOffences = deletedOffences;
            return this;
        }

        public Builder withAddedOffences(final List<DefendantOffences> addedOffences) {
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