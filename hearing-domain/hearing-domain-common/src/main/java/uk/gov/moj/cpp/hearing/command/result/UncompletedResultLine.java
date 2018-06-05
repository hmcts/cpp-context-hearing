package uk.gov.moj.cpp.hearing.command.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public final class UncompletedResultLine implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;

    private final UUID resultDefinitionId;

    private final UUID defendantId;

    @JsonCreator
    private UncompletedResultLine(@JsonProperty("id") final UUID id,
                                  @JsonProperty("resultDefinitionId") final UUID resultDefinitionId,
                                  @JsonProperty("defendantId") final UUID defendantId) {
        this.id = id;
        this.resultDefinitionId = resultDefinitionId;
        this.defendantId = defendantId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getResultDefinitionId() {
        return resultDefinitionId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;

        private UUID resultDefinitionId;

        private UUID defendantId;

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withResultDefinitionId(final UUID resultDefinitionId) {
            this.resultDefinitionId = resultDefinitionId;
            return this;
        }

        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public UncompletedResultLine build() {
            return new UncompletedResultLine(id, resultDefinitionId, defendantId);
        }
    }
}