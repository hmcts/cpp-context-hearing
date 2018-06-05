package uk.gov.moj.cpp.hearing.command.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

public final class CompletedResultLine implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;

    private UUID resultDefinitionId;

    private final UUID lastSharedResultId;

    private final UUID caseId;

    private UUID defendantId;

    private final UUID offenceId;

    private final Level level;

    private final String resultLabel;

    private final List<ResultPrompt> prompts;

    @JsonCreator
    private CompletedResultLine(@JsonProperty("id") final UUID id,
                                @JsonProperty("resultDefinitionId") final UUID resultDefinitionId,
                                @JsonProperty("lastSharedResultId") final UUID lastSharedResultId,
                                @JsonProperty("caseId") final UUID caseId,
                                @JsonProperty("defendantId") final UUID defendantId,
                                @JsonProperty("offenceId") final UUID offenceId,
                                @JsonProperty("level") final Level level,
                                @JsonProperty("resultLabel") final String resultLabel,
                                @JsonProperty("prompts") final List<ResultPrompt> prompts) {
        this.id = id;
        this.resultDefinitionId = resultDefinitionId;
        this.lastSharedResultId = lastSharedResultId;
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.offenceId = offenceId;
        this.level = level;
        this.resultLabel = resultLabel;
        this.prompts = unmodifiableList(ofNullable(prompts).orElseGet(ArrayList::new));
    }

    public UUID getId() {
        return id;
    }

    public UUID getResultDefinitionId() {
        return resultDefinitionId;
    }

    public UUID getLastSharedResultId() {
        return lastSharedResultId;
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

    public static Builder builder() {
        return new Builder();
    }

    public void setResultDefinitionId(UUID resultDefinitionId) {
        this.resultDefinitionId = resultDefinitionId;
    }

    public void setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
    }

    public static final class Builder {

        private UUID id;

        private UUID resultDefinitionId;

        private UUID lastSharedResultId;

        private UUID caseId;

        private UUID defendantId;

        private UUID offenceId;

        private Level level;

        private String resultLabel;

        private List<ResultPrompt> prompts;

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withResultDefinitionId(final UUID resultDefinitionId) {
            this.resultDefinitionId = resultDefinitionId;
            return this;
        }

        public Builder withLastSharedResultId(final UUID lastSharedResultId) {
            this.lastSharedResultId = lastSharedResultId;
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

        public CompletedResultLine build() {
            return new CompletedResultLine(id, resultDefinitionId, lastSharedResultId, caseId, defendantId, offenceId, level, resultLabel, prompts);
        }
    }
}
