package uk.gov.moj.cpp.hearing.event.nows.domain;

import java.io.Serializable;

public class NowOrderPrompt implements Serializable {

    private final static long serialVersionUID = -2080535270112882879L;
    private String label;
    private String value;

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public NowOrderPrompt(String label, String value) {
        this.label = label;
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

        public NowOrderPrompt build() {
            NowOrderPrompt prompt = new NowOrderPrompt(label, value);
            return prompt;
        }
    }
}
