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

    public String getLanguage() {
        return language;
    }

    public Interpreter setLanguage(String language) {
        this.language = language;
        return this;
    }

    public static Interpreter interpreter() {
        return new Interpreter();
    }
}
