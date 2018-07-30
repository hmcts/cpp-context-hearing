package uk.gov.moj.cpp.hearing.command.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public final class ResultPrompt implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;

    private final String label;

    private final String value;

    @JsonCreator
    private ResultPrompt(@JsonProperty("id") final UUID id,
                         @JsonProperty("label") final String label,
                         @JsonProperty("value") final String value) {
        this.id = id;
        this.label = label;
        this.value = value;
    }

    public UUID getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;

        private String label;

        private String value;

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withLabel(final String label) {
            this.label = label;
            return this;
        }

        public Builder withValue(final String value) {
            this.value = value;
            return this;
        }

        public ResultPrompt build() {
            return new ResultPrompt(id, label, value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResultPrompt resultPrompt = (ResultPrompt) o;
        return Objects.equals(id, resultPrompt.id) &&
                Objects.equals(label, resultPrompt.label) &&
                Objects.equals(value, resultPrompt.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, value);
    }
}
