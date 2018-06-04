package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class Interpreter {

    private String name;

    private String language;

    public static Interpreter interpreter() {
        return new Interpreter();
    }

    public String getName() {
        return this.name;
    }

    public Interpreter setName(String name) {
        this.name = name;
        return this;
    }

    public String getLanguage() {
        return this.language;
    }

    public Interpreter setLanguage(String language) {
        this.language = language;
        return this;
    }
}
