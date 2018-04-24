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
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
public final class Result implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final UUID lastSharedResultId;
    private final UUID resultLineId;
    private final String originalText;
    private final String resultLevel;
    private final Boolean isCompleted;
    private final List<Part> parts;
    private final List<Choice> choices;

    @JsonCreator
    protected Result(@JsonProperty("id") final UUID id,
            @JsonProperty("lastSharedResultId") final UUID lastSharedResultId, 
            @JsonProperty("resultLineId") final UUID resultLineId, 
            @JsonProperty("originalText") final String originalText, 
            @JsonProperty("resultLevel") final String resultLevel, 
            @JsonProperty("isCompleted") final Boolean isCompleted, 
            @JsonProperty("parts") final List<Part> parts, 
            @JsonProperty("choices") final List<Choice> choices) {
        this.id = id;
        this.lastSharedResultId = lastSharedResultId;
        this.resultLineId = resultLineId;
        this.originalText = originalText;
        this.resultLevel = resultLevel;
        this.isCompleted = isCompleted;
        this.parts = parts;
        this.choices = unmodifiableList(ofNullable(choices).orElseGet(ArrayList::new));
    }

    @JsonIgnore
    private Result(final Builder builder) {
        this.id = builder.id;
        this.lastSharedResultId = builder.lastSharedResultId;
        this.resultLineId = builder.resultLineId;
        this.originalText = builder.originalText;
        this.resultLevel = builder.resultLevel;
        this.isCompleted = builder.isCompleted;
        this.parts = builder.parts;
        this.choices = unmodifiableList(ofNullable(builder.choices).orElseGet(ArrayList::new));
    }

    public UUID getResultDefinitionId() {
        return getId();
    }

    public UUID getId() {
        return id;
    }

    public UUID getLastSharedResultId() {
        return lastSharedResultId;
    }

    public UUID getResultLineId() {
        return resultLineId;
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getResultLevel() {
        return resultLevel;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public List<Part> getParts() {
        return parts;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Result that = (Result) o;
        return Objects.equals(this.lastSharedResultId, that.lastSharedResultId)
                && Objects.equals(this.resultLineId, that.resultLineId)
                && Objects.equals(this.originalText, that.originalText)
                && Objects.equals(this.id, that.id)
                && Objects.equals(this.resultLevel, that.resultLevel)
                && Objects.equals(this.isCompleted, that.isCompleted) && Objects.equals(this.parts, that.parts)
                && Objects.equals(this.choices, that.choices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.lastSharedResultId, this.resultLineId, this.originalText, this.id,
                this.resultLevel, this.isCompleted, this.parts, this.choices);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private UUID lastSharedResultId;
        private UUID resultLineId;
        private String originalText;
        private String resultLevel;
        private Boolean isCompleted;
        private List<Part> parts;
        private List<Choice> choices;

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withLastSharedResultId(final UUID lastSharedResultId) {
            this.lastSharedResultId = lastSharedResultId;
            return this;
        }

        public Builder withResultLineId(final UUID resultLineId) {
            this.resultLineId = resultLineId;
            return this;
        }

        public Builder withOriginalText(final String originalText) {
            this.originalText = originalText;
            return this;
        }

        public Builder withResultLevel(final String resultLevel) {
            this.resultLevel = resultLevel;
            return this;
        }

        public Builder withIsCompleted(final Boolean isCompleted) {
            this.isCompleted = isCompleted;
            return this;
        }

        public Builder withParts(final List<Part> parts) {
            this.parts = parts;
            return this;
        }

        public Builder withChoices(final List<Choice> choices) {
            this.choices = choices;
            return this;
        }

        public Result build() {
            return new Result(this);
        }
    }
}