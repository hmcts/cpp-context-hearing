package uk.gov.moj.cpp.hearing.message.shareResults;

import java.util.UUID;

public class Prompt {

    private UUID id;
    private String label;
    private String value;

    public static Prompt prompt() {
        return new Prompt();
    }

    public UUID getId() {
        return id;
    }

    public Prompt setId(final UUID id) {
        this.id = id;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public Prompt setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Prompt setValue(String value) {
        this.value = value;
        return this;
    }
}
