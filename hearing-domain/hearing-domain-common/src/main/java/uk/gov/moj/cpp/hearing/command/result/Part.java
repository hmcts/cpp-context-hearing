package uk.gov.moj.cpp.hearing.command.result;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
public final class Part implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String value;
    private final String type;
    private final String state;
    private final List<Choice> resultChoices; // choice is resultChoice?
    private final String code;
    private final String label;

    @JsonCreator
    protected Part(@JsonProperty("value") final String value, 
            @JsonProperty("type") final String type, 
            @JsonProperty("state") final String state, 
            @JsonProperty("resultChoices") final List<Choice> resultChoices, 
            @JsonProperty("code") final String code, 
            @JsonProperty("label") final String label) {
        this.value = value;
        this.type = type;
        this.state = state;
        this.resultChoices = unmodifiableList(ofNullable(resultChoices).orElseGet(ArrayList::new));
        this.code = code;
        this.label = label;
    }

    @JsonIgnore
    private Part(final Builder builder) {
        this.value = builder.value;
        this.type = builder.type;
        this.state = builder.state;
        this.resultChoices =  unmodifiableList(ofNullable(builder.resultChoices).orElseGet(ArrayList::new));
        this.code = builder.code;
        this.label = builder.label;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String getState() {
        return state;
    }

    public List<Choice> getResultChoices() {
        return resultChoices;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Part that = (Part) o;
        return Objects.equals(this.value, that.value)
                && Objects.equals(this.type, that.type)
                && Objects.equals(this.state, that.state)
                && Objects.equals(this.resultChoices, that.resultChoices)
                && Objects.equals(this.code, that.code)
                && Objects.equals(this.label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value, this.type, this.state, this.resultChoices,
                this.code, this.label);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String value;
        private String type;
        private String state;
        private List<Choice> resultChoices;
        private String code;
        private String label;

        public Builder withValue(final String value) {
            this.value = value;
            return this;
        }

        public Builder withType(final String type) {
            this.type = type;
            return this;
        }

        public Builder withState(final String state) {
            this.state = state;
            return this;
        }

        public Builder withResultChoices(final List<Choice> resultChoices) {
            this.resultChoices = resultChoices;
            return this;
        }

        public Builder withCode(final String code) {
            this.code = code;
            return this;
        }

        public Builder withLabel(final String label) {
            this.label = label;
            return this;
        }
        
        public Part build() {
            return new Part(this);
        }
    }
}