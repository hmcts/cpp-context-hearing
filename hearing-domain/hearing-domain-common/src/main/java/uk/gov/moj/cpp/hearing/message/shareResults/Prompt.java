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

    public void setLabel(String label) {
        this.label = label;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static Prompt prompt(){
        return new Prompt();
    }
}
