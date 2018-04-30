package uk.gov.moj.cpp.hearing.command.defendant;

import java.io.Serializable;

public class Interpreter implements Serializable {

    private final static long serialVersionUID = 1L;

    private String language;

    private Boolean needed;

    public Interpreter() {
    }

    public Interpreter(final String language, final Boolean needed) {
        this.language = language;
        this.needed = needed;
    }

    public static Builder interpreter() {
        return new Interpreter.Builder();
    }

    public String getLanguage() {
        return language;
    }

    public Boolean getNeeded() {
        return needed;
    }

    public static class Builder {

        private String language;

        private Boolean needed;

        private Builder() {
        }

        public Builder withLanguage(final String language) {
            this.language = language;
            return this;
        }

        public Builder withNeeded(final Boolean needed) {
            this.needed = needed;
            return this;
        }

        public Interpreter build() {
            return new Interpreter(language, needed);
        }
    }
}
