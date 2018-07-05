package uk.gov.moj.cpp.hearing.command.offence;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class AddedOffence {

    private UUID defendantId;

    private UUID caseId;

    private final List<DefendantOffence> offences;

    @JsonCreator
    public AddedOffence(@JsonProperty("defendantId") final UUID defendantId,
                        @JsonProperty("caseId") final UUID caseId,
                        @JsonProperty("offences") final List<DefendantOffence> offences) {
        this.defendantId = defendantId;
        this.caseId = caseId;
        this.offences = offences;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public AddedOffence setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public AddedOffence setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public List<DefendantOffence> getOffences() {
        return offences;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID defendantId;

        private UUID caseId;

        private List<DefendantOffence> addedOffences;

        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withAddedOffences(final List<DefendantOffence> offences) {
            this.addedOffences = offences;
            return this;
        }

        public AddedOffence build() {
            return new AddedOffence(defendantId, caseId, ofNullable(addedOffences).orElse(new ArrayList<>()));
        }
    }
}
