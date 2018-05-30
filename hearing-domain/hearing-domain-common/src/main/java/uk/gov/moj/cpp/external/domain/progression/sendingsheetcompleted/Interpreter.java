package uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Interpreter implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String language;

    private final Boolean needed;

    @JsonCreator
    public Interpreter(
            @JsonProperty("language") final String language,
            @JsonProperty("needed") final Boolean needed) {
        this.language = language;
        this.needed = needed;
    }

    public String getLanguage() {
        return language;
    }

    public Boolean getNeeded() {
        return needed;
    }

    public static Builder interpreter() {
        return new Interpreter.Builder();
    }

    public static class Builder {
        private String language;

        private Boolean needed;

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
