package uk.gov.moj.cpp.hearing.message.shareResults;

public class Prompt {

    private String label;
    private String value;

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public Prompt setLabel(String label) {
        this.label = label;
        return this;
    }

    public Prompt setValue(String value) {
        this.value = value;
        return this;
    }

    public static Prompt prompt() {
        return new Prompt();
    }
}
