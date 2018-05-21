package uk.gov.moj.cpp.hearing.command.defendant;

import java.io.Serializable;

public class Interpreter implements Serializable {

    private final static long serialVersionUID = 1L;

    private String language;

    public Interpreter() {
    }

    public Interpreter(final String language) {
        this.language = language;
    }

    public static Builder builder() {
        return new Interpreter.Builder();
    }

    public static Builder builder(String language) {
        return Interpreter.builder()
                .withLanguage(language);
    }

    public String getLanguage() {
        return language;
    }

    public static class Builder {

        private String language;

        private Builder() {
        }

        public Builder withLanguage(final String language) {
            this.language = language;
            return this;
        }

        public Interpreter build() {
            return new Interpreter(language);
        }
    }
}
