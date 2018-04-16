package uk.gov.moj.cpp.hearing.command.initiate;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Interpreter implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean needed;
    private final String language;

    @JsonCreator
    public Interpreter(@JsonProperty("needed") final boolean needed,
                       @JsonProperty("language") final String language) {
        this.needed = needed;
        this.language = language;
    }

    public boolean isNeeded() {
        return needed;
    }

    public String getLanguage() {
        return language;
    }

    public static class Builder {

        private boolean needed;
        private String language;

        private Builder() {

        }

        public boolean isNeeded() {
            return needed;
        }

        public String getLanguage() {
            return language;
        }

        public Builder withNeeded(boolean needed) {
            this.needed = needed;
            return this;
        }

        public Builder withLanguage(String language) {
            this.language = language;
            return this;
        }

        public Interpreter build() {
            return new Interpreter(needed, language);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(Interpreter interpreter) {
        return builder()
                .withNeeded(interpreter.isNeeded())
                .withLanguage(interpreter.getLanguage());
    }
}
