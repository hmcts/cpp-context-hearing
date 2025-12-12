package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;

public class Interpreter implements Serializable {

    private final static long serialVersionUID = -551447889707487307L;
    private String name;
    private String language;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public static final class Builder {
        private String name;
        private String language;


        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withLanguage(String language) {
            this.language = language;
            return this;
        }

        public Interpreter build() {
            Interpreter interpreter = new Interpreter();
            interpreter.setName(name);
            interpreter.setLanguage(language);
            return interpreter;
        }
    }
}
