package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;
import java.util.UUID;


public class Prompt implements Serializable {

    private final static long serialVersionUID = -7063185622725100131L;
    private UUID id;
    private String label;
    private String value;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public static final class Builder {
        private UUID id;
        private String label;
        private String value;

        private Builder() {
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Prompt build() {
            final Prompt prompt = new Prompt();
            prompt.setLabel(label);
            prompt.setValue(value);
            prompt.setId(id);
            return prompt;
        }
    }
}
