package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class PromptRefs {

    private String label;

    public static PromptRefs promptRefs() {
        return new PromptRefs();
    }

    public String getLabel() {
        return this.label;
    }

    public PromptRefs setLabel(String label) {
        this.label = label;
        return this;
    }
}
