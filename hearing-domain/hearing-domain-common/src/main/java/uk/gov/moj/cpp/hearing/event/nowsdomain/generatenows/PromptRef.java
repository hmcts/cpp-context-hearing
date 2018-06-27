package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.util.UUID;

public class PromptRef implements Serializable {

    private UUID id;

    private String label;

    public static PromptRef promptRef() {
        return new PromptRef();
    }

    public UUID getId() {
        return this.id;
    }

    public PromptRef setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getLabel() {
        return this.label;
    }

    public PromptRef setLabel(String label) {
        this.label = label;
        return this;
    }
}
