package uk.gov.moj.cpp.hearing.command.result;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ResultLine implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID resultDefinitionId;
    private UUID lastSharedResultId;
    private UUID caseId;
    private UUID personId;
    private UUID offenceId;
    private Level level;
    private String resultLabel;
    private Boolean complete;
    private UUID clerkOfTheCourtId;
    private String clerkOfTheCourtFirstName;
    private String clerkOfTheCourtLastName;
    private List<ResultPrompt> prompts;

    @JsonCreator
    protected ResultLine(@JsonProperty("id") final UUID id,
                         @JsonProperty("resultDefinitionId") final UUID resultDefinitionId,
                         @JsonProperty("lastSharedResultId") final UUID lastSharedResultId,
                         @JsonProperty("caseId") final UUID caseId,
                         @JsonProperty("personId") final UUID personId,
                         @JsonProperty("offenceId") final UUID offenceId,
                         @JsonProperty("level") final Level level,
                         @JsonProperty("resultLabel") final String resultLabel,
                         @JsonProperty("complete") final Boolean complete,
                         @JsonProperty("clerkOfTheCourtId") final UUID clerkOfTheCourtId,
                         @JsonProperty("clerkOfTheCourtFirstName") final String clerkOfTheCourtFirstName,
                         @JsonProperty("clerkOfTheCourtLastName") final String clerkOfTheCourtLastName,
                         @JsonProperty("prompts") final List<ResultPrompt> prompts) {
        this.id = id;
        this.resultDefinitionId = resultDefinitionId;
        this.lastSharedResultId = lastSharedResultId;
        this.caseId = caseId;
        this.personId = personId;
        this.offenceId = offenceId;
        this.level = level;
        this.resultLabel = resultLabel;
        this.complete = complete;
        this.clerkOfTheCourtId = clerkOfTheCourtId;
        this.clerkOfTheCourtFirstName = clerkOfTheCourtFirstName;
        this.clerkOfTheCourtLastName = clerkOfTheCourtLastName;
        this.prompts = unmodifiableList(ofNullable(prompts).orElseGet(ArrayList::new));
    }

    @JsonIgnore
    private ResultLine(final Builder builder) {
        this.id = builder.id;
        this.resultDefinitionId = builder.resultDefinitionId;
        this.lastSharedResultId = builder.lastSharedResultId;
        this.caseId = builder.caseId;
        this.personId = builder.personId;
        this.offenceId = builder.offenceId;
        this.level = builder.level;
        this.resultLabel = builder.resultLabel;
        this.complete = builder.complete;
        this.clerkOfTheCourtId = builder.clerkOfTheCourtId;
        this.clerkOfTheCourtFirstName = builder.clerkOfTheCourtFirstName;
        this.clerkOfTheCourtLastName = builder.clerkOfTheCourtLastName;
        this.prompts = unmodifiableList(ofNullable(builder.prompts).orElseGet(ArrayList::new));
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

    public UUID getPersonId() {
        return personId;
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

    public Boolean getComplete() {
        return isComplete();
    }

    public Boolean isComplete() {
        return null != complete && complete;
    }

    public UUID getClerkOfTheCourtId() {
        return clerkOfTheCourtId;
    }

    public String getClerkOfTheCourtFirstName() {
        return clerkOfTheCourtFirstName;
    }

    public String getClerkOfTheCourtLastName() {
        return clerkOfTheCourtLastName;
    }

    public List<ResultPrompt> getPrompts() {
        return prompts;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setResultDefinitionId(UUID resultDefinitionId) {
        this.resultDefinitionId = resultDefinitionId;
    }

    public void setLastSharedResultId(UUID lastSharedResultId) {
        this.lastSharedResultId = lastSharedResultId;
    }

    public void setCaseId(UUID caseId) {
        this.caseId = caseId;
    }

    public void setPersonId(UUID personId) {
        this.personId = personId;
    }

    public void setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void setResultLabel(String resultLabel) {
        this.resultLabel = resultLabel;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    public void setClerkOfTheCourtId(UUID clerkOfTheCourtId) {
        this.clerkOfTheCourtId = clerkOfTheCourtId;
    }

    public void setClerkOfTheCourtFirstName(String clerkOfTheCourtFirstName) {
        this.clerkOfTheCourtFirstName = clerkOfTheCourtFirstName;
    }

    public void setClerkOfTheCourtLastName(String clerkOfTheCourtLastName) {
        this.clerkOfTheCourtLastName = clerkOfTheCourtLastName;
    }

    public void setPrompts(List<ResultPrompt> prompts) {
        this.prompts = prompts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResultLine that = (ResultLine) o;
        return Objects.equals(this.id, that.id) && Objects.equals(this.resultDefinitionId, that.resultDefinitionId)
                && Objects.equals(this.lastSharedResultId, that.lastSharedResultId)
                && Objects.equals(this.caseId, that.caseId) && Objects.equals(this.personId, that.personId)
                && Objects.equals(this.offenceId, that.offenceId) && Objects.equals(this.level, that.level)
                && Objects.equals(this.resultLabel, that.resultLabel) && Objects.equals(this.complete, that.complete)
                && Objects.equals(this.clerkOfTheCourtId, that.clerkOfTheCourtId)
                && Objects.equals(this.clerkOfTheCourtFirstName, that.clerkOfTheCourtFirstName)
                && Objects.equals(this.clerkOfTheCourtLastName, that.clerkOfTheCourtLastName)
                && Objects.equals(this.prompts, that.prompts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.lastSharedResultId, this.caseId, this.personId, this.offenceId, this.level,
                this.resultLabel, this.clerkOfTheCourtId, this.clerkOfTheCourtFirstName, this.clerkOfTheCourtLastName, this.prompts);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private UUID resultDefinitionId;
        private UUID lastSharedResultId;
        private UUID caseId;
        private UUID personId;
        private UUID offenceId;
        private Level level;
        private String resultLabel;
        private Boolean complete;
        private UUID clerkOfTheCourtId;
        private String clerkOfTheCourtFirstName;
        private String clerkOfTheCourtLastName;
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

        public Builder withPersonId(final UUID personId) {
            this.personId = personId;
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


        public Builder withComplete(final Boolean complete) {
            this.complete = complete;
            return this;
        }

        public Builder withClerkOfTheCourtId(final UUID clerkOfTheCourtId) {
            this.clerkOfTheCourtId = clerkOfTheCourtId;
            return this;
        }

        public Builder withClerkOfTheCourtFirstName(final String clerkOfTheCourtFirstName) {
            this.clerkOfTheCourtFirstName = clerkOfTheCourtFirstName;
            return this;
        }

        public Builder withClerkOfTheCourtLastName(final String clerkOfTheCourtLastName) {
            this.clerkOfTheCourtLastName = clerkOfTheCourtLastName;
            return this;
        }

        public Builder withPrompts(final List<ResultPrompt> prompts) {
            this.prompts = prompts;
            return this;
        }

        public ResultLine build() {
            return new ResultLine(this);
        }
    }
}