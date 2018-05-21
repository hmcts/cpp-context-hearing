package uk.gov.justice.progression.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.offence.DefendantCaseOffence;
import uk.gov.moj.cpp.hearing.command.offence.Offence;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Event("public.events.public-case-defendant-offences-changed")
public final class CaseDefendantOffencesChanged {

    private final LocalDate modifiedDate;

    private final List<Offence> updatedOffences;

    private final List<UUID> deletedOffences;

    private final List<DefendantCaseOffence> addedOffences;

    @JsonCreator
    private CaseDefendantOffencesChanged(@JsonProperty("modifiedDate") final LocalDate modifiedDate,
                                        @JsonProperty("addedOffences") final List<DefendantCaseOffence> addedOffences,
                                        @JsonProperty("updatedOffences") final List<Offence> updatedOffences,
                                        @JsonProperty("deletedOffences") final List<UUID> deletedOffences) {
        this.modifiedDate = modifiedDate;

        this.updatedOffences = nonNull(updatedOffences) ? new ArrayList<>(updatedOffences) : new ArrayList<>();

        this.deletedOffences = nonNull(deletedOffences) ? new ArrayList<>(deletedOffences) : new ArrayList<>();

        this.addedOffences = nonNull(addedOffences) ? new ArrayList<>(addedOffences) : new ArrayList<>();
    }

    public LocalDate getModifiedDate() {
        return modifiedDate;
    }

    public List<Offence> getUpdatedOffences() {
        return new ArrayList<>(updatedOffences);
    }

    public List<UUID> getDeletedOffences() {
        return new ArrayList<>(deletedOffences);
    }

    public List<DefendantCaseOffence> getAddedOffences() {
        return new ArrayList<>(addedOffences);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private LocalDate modifiedDate;

        private List<Offence> updatedOffences;

        private List<UUID> deletedOffences;

        private List<DefendantCaseOffence> addedOffences;

        public Builder withModifiedDate(final LocalDate modifiedDate) {
            this.modifiedDate = modifiedDate;
            return  this;
        }

        public Builder withUpdateOffences(final List<Offence> updatedOffences) {
            this.updatedOffences = updatedOffences;
            return  this;
        }

        public Builder withDeletedOffences(final List<UUID> deletedOffences) {
            this.deletedOffences = deletedOffences;
            return  this;
        }

        public Builder withAddedOffences(final List<DefendantCaseOffence> addedOffences) {
            this.addedOffences = addedOffences;
            return  this;
        }

        public CaseDefendantOffencesChanged build() {
            return new CaseDefendantOffencesChanged(modifiedDate,
                    ofNullable(addedOffences).orElse(new ArrayList<>()),
                    ofNullable(updatedOffences).orElse(new ArrayList<>()),
                    ofNullable(deletedOffences).orElse(new ArrayList<>()));
        }
    }
}