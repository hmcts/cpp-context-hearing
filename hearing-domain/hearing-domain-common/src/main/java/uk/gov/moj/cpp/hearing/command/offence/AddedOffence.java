package uk.gov.moj.cpp.hearing.command.offence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;

public final class AddedOffence {

    private UUID defendantId;

    private UUID caseId;

    private final List<UpdatedOffence> offences;

    @JsonCreator
    public AddedOffence(@JsonProperty("defendantId") final UUID defendantId,
                        @JsonProperty("caseId") final UUID caseId,
                        @JsonProperty("offences") final List<UpdatedOffence> offences) {
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

    public List<UpdatedOffence> getOffences() {
        return offences;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID defendantId;

        private UUID caseId;

        private List<UpdatedOffence> addedOffences;

        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withAddedOffences(final List<UpdatedOffence> offences) {
            this.addedOffences = offences;
            return this;
        }

        public AddedOffence build() {
            return new AddedOffence(defendantId, caseId, ofNullable(addedOffences).orElse(new ArrayList<>()));
        }
    }
}
