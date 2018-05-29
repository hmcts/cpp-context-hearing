package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class Prompts {

    private String label;

    private String value;

    public static Prompts prompts() {
        return new Prompts();
    }

    public String getLabel() {
        return this.label;
    }

    public Prompts setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getValue() {
        return this.value;
    }

    public Prompts setValue(String value) {
        this.value = value;
        return this;
    }
}
