package uk.gov.moj.cpp.hearing.command.result;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
public final class ResultPrompt implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String label;
    private final String value;

    @JsonCreator
    protected ResultPrompt(@JsonProperty("label") final String label, 
            @JsonProperty("value") final String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResultPrompt that = (ResultPrompt) o;
        return Objects.equals(this.label, that.label) &&
                Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.label, this.value);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        
        private String label;
        private String value;

        public Builder withLabel(final String label) {
            this.label = label;
            return this;
        }
        
        public Builder withValue(final String value) {
            this.value = value;
            return this;
        }
        
        public ResultPrompt build() {
            return new ResultPrompt(this.label, this.value);
        }
    }
}
