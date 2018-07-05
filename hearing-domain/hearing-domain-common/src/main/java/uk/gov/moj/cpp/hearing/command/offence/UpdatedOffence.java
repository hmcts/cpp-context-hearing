package uk.gov.moj.cpp.hearing.command.offence;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdatedOffence {
    private UUID defendantId;

    private UUID caseId;

    private final List<BaseDefendantOffence> offences;

    @JsonCreator
    public UpdatedOffence(@JsonProperty("defendantId") final UUID defendantId,
                        @JsonProperty("caseId") final UUID caseId,
                        @JsonProperty("offences") final List<BaseDefendantOffence> offences) {
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

    public UpdatedOffence setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public UpdatedOffence setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public List<BaseDefendantOffence> getOffences() {
        return offences;
    }

    public static UpdatedOffence.Builder builder() {
        return new UpdatedOffence.Builder();
    }

    public static class Builder {

        private UUID defendantId;

        private UUID caseId;

        private List<BaseDefendantOffence> updatedOffences;

        public UpdatedOffence.Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public UpdatedOffence.Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public UpdatedOffence.Builder withUpdatedOffences(final List<BaseDefendantOffence> offences) {
            this.updatedOffences = offences;
            return this;
        }

        public UpdatedOffence build() {
            return new UpdatedOffence(defendantId, caseId, ofNullable(updatedOffences).orElse(new ArrayList<>()));
        }
    }
}

