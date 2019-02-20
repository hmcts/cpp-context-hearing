package uk.gov.moj.cpp.hearing.command.initiate;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Interpreter implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean needed;
    private String language;

    public Interpreter() {
    }

    @JsonCreator
    public Interpreter(@JsonProperty("needed") final boolean needed,
                       @JsonProperty("language") final String language) {
        this.needed = needed;
        this.language = language;
    }

    public static Interpreter interpreter() {
        return new Interpreter();
    }

    public boolean isNeeded() {
        return needed;
    }

    public Interpreter setNeeded(boolean needed) {
        this.needed = needed;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public Interpreter setLanguage(String language) {
        this.language = language;
        return this;
    }
}
