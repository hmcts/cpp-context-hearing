package uk.gov.moj.cpp.hearing.message.shareResults;

public class Interpreter {

    private String name;
    private String language;

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public Interpreter setName(String name) {
        this.name = name;
        return this;
    }

    public Interpreter setLanguage(String language) {
        this.language = language;
        return this;
    }

    public static Interpreter interpreter(){
        return new Interpreter();
    }
}
