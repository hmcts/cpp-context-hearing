package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Prompt implements Serializable {

    private final static long serialVersionUID = -7063185622725100131L;
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
    public static final class Builder {
        private String label;
        private String value;

        private Builder() {
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
            Prompt prompt = new Prompt();
            prompt.setLabel(label);
            prompt.setValue(value);
            return prompt;
        }
    }
}
