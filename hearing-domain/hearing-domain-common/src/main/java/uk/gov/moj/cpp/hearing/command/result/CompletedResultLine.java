package uk.gov.moj.cpp.hearing.command.result;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("squid:S1067")
public final class CompletedResultLine implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;

    private UUID resultDefinitionId;

    private final UUID caseId;

    private UUID defendantId;

    private final UUID offenceId;

    private final Level level;

    private final String resultLabel;

    private List<ResultPrompt> prompts;

    private LocalDate orderedDate;

    @JsonCreator
    private CompletedResultLine(@JsonProperty("id") final UUID id,
                                @JsonProperty("resultDefinitionId") final UUID resultDefinitionId,
                                @JsonProperty("caseId") final UUID caseId,
                                @JsonProperty("defendantId") final UUID defendantId,
                                @JsonProperty("offenceId") final UUID offenceId,
                                @JsonProperty("level") final Level level,
                                @JsonProperty("resultLabel") final String resultLabel,
                    @JsonProperty("prompts") final List<ResultPrompt> prompts,
                    @JsonProperty("orderedDate") final LocalDate orderedDate) {
        this.id = id;
        this.resultDefinitionId = resultDefinitionId;
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.offenceId = offenceId;
        this.level = level;
        this.resultLabel = resultLabel;
        this.prompts = unmodifiableList(ofNullable(prompts).orElseGet(ArrayList::new));
        this.orderedDate = orderedDate;
    }

    public UUID getId() {
        return id;
    }

    public UUID getResultDefinitionId() {
        return resultDefinitionId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public Level getLevel() {
        return level;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public List<ResultPrompt> getPrompts() {
        return new ArrayList<>(prompts);
    }

    public LocalDate getOrderedDate() {
        return orderedDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void setResultDefinitionId(final UUID resultDefinitionId) {
        this.resultDefinitionId = resultDefinitionId;
    }

    public void setDefendantId(final UUID defendantId) {
        this.defendantId = defendantId;
    }

    public void setOrderedDate(final LocalDate orderedDate){
        this.orderedDate = orderedDate;
    }

    public void setPrompts(List<ResultPrompt> prompts) {
        this.prompts = new ArrayList<>(prompts);
    }

    public static final class Builder {

        private UUID id;

        private UUID resultDefinitionId;

        private UUID caseId;

        private UUID defendantId;

        private UUID offenceId;

        private Level level;

        private String resultLabel;

        private List<ResultPrompt> prompts;

        private LocalDate orderedDate;

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withResultDefinitionId(final UUID resultDefinitionId) {
            this.resultDefinitionId = resultDefinitionId;
            return this;
        }

        public Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public Builder withLevel(final Level level) {
            this.level = level;
            return this;
        }

        public Builder withResultLabel(final String resultLabel) {
            this.resultLabel = resultLabel;
            return this;
        }

        public Builder withResultPrompts(final List<ResultPrompt> prompts) {
            this.prompts = new ArrayList<>(prompts);
            return this;
        }

        public Builder withOrderedDate(final LocalDate orderedDate) {
            this.orderedDate = orderedDate;
            return this;
        }
        public CompletedResultLine build() {
            return new CompletedResultLine(id, resultDefinitionId, caseId, defendantId, offenceId,
                            level, resultLabel, prompts, orderedDate);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CompletedResultLine completedResultLine = (CompletedResultLine) o;
        return Objects.equals(id, completedResultLine.id) &&
                Objects.equals(resultDefinitionId, completedResultLine.resultDefinitionId) &&
                Objects.equals(caseId, completedResultLine.caseId) &&
                Objects.equals(defendantId, completedResultLine.defendantId) &&
                Objects.equals(offenceId, completedResultLine.offenceId) &&
                Objects.equals(level, completedResultLine.level) &&
                Objects.equals(resultLabel, completedResultLine.resultLabel) &&
                Objects.equals(prompts, completedResultLine.prompts) &&
                Objects.equals(orderedDate, completedResultLine.orderedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, resultDefinitionId, caseId, defendantId,
                offenceId, level, resultLabel, prompts, orderedDate);
    }
}
