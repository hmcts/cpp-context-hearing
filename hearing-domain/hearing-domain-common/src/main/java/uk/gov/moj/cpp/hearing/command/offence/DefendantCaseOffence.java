package uk.gov.moj.cpp.hearing.command.offence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;

public final class DefendantCaseOffence {

    private final UUID defendantId;

    private final UUID caseId;

    private final List<Offence> addedOffences;

    @JsonCreator
    public DefendantCaseOffence(@JsonProperty("defendantId") final UUID defendantId,
                                @JsonProperty("caseId") final UUID caseId,
                                @JsonProperty("addedOffences")final List<Offence> addedOffences) {
        this.defendantId = defendantId;
        this.caseId = caseId;
        this.addedOffences = new ArrayList<>(addedOffences);
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public List<Offence> getAddedOffences() {
        return new ArrayList<>(addedOffences);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID defendantId;

        private UUID caseId;

        private List<Offence> addedOffences;

        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withAddedOffences(final List<Offence> offences) {
            this.addedOffences = offences;
            return this;
        }

        public DefendantCaseOffence build() {
            return new DefendantCaseOffence(defendantId, caseId, ofNullable(addedOffences).orElse(new ArrayList<>()));
        }
    }
}
