package uk.gov.moj.cpp.hearing.command.result;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
public final class Choice implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final String code;
    private final String label;
    private final String type;
    private final Boolean required;

    @JsonCreator
    protected Choice(@JsonProperty("code") final String code, 
            @JsonProperty("label") final String label, 
            @JsonProperty("type") final String type, 
            @JsonProperty("required") final Boolean required) {
        this.code = code;
        this.label = label;
        this.type = type;
        this.required = required;
    }

    @JsonIgnore
    private Choice(final Builder builder) {
        this.code = builder.code;
        this.label = builder.label;
        this.type = builder.type;
        this.required = builder.required;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public Boolean getRequired() {
        return required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Choice that = (Choice) o;
        return Objects.equals(this.code, that.code)
                && Objects.equals(this.label, that.label)
                && Objects.equals(this.type, that.type)
                && Objects.equals(this.required, that.required);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.code, this.label, this.type, this.required);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        
        private String code;
        private String label;
        private String type;
        private Boolean required;
        
        public Builder withCode(final String code) {
            this.code = code;
            return this;
        }
        
        public Builder withLabel(final String label) {
            this.label = label;
            return this;
        }
        
        public Builder withType(final String type) {
            this.type = type;
            return this;
        }
        
        public Builder withRequired(final Boolean required) {
            this.required = required;
            return this;
        }

        public Choice build() {
            return new Choice(this);
        }
    }
}