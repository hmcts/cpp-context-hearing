package uk.gov.moj.cpp.hearing.command.result;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class UncompletedResultLine implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;

    private final UUID resultDefinitionId;

    private final UUID defendantId;

    private final LocalDate orderedDate;

    @JsonCreator
    private UncompletedResultLine(@JsonProperty("id") final UUID id,
                                  @JsonProperty("resultDefinitionId") final UUID resultDefinitionId,
                                  @JsonProperty("defendantId") final UUID defendantId,
                                  @JsonProperty("orderedDate") final LocalDate orderedDate) {
        this.id = id;
        this.resultDefinitionId = resultDefinitionId;
        this.defendantId = defendantId;
        this.orderedDate = orderedDate;
    }

    public static Builder builder() {
        return new Builder();
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

    public LocalDate getOrderedDate() {
        return orderedDate;
    }

    public static final class Builder {

        private UUID id;

        private UUID resultDefinitionId;

        private UUID defendantId;

        private LocalDate dateOrdered;

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

        public Builder withOrderedDate(final LocalDate dateOrdered) {
            this.dateOrdered = dateOrdered;
            return this;
        }

        public UncompletedResultLine build() {
            return new UncompletedResultLine(id, resultDefinitionId, defendantId, dateOrdered);
        }
    }
}