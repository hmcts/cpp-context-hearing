package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.util.UUID;

//TODO GPE-6313 remove
@SuppressWarnings({"squid:S1135"})
public class Prompts implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private String label;

    private String value;

    public static Prompts prompts() {
        return new Prompts();
    }

    public UUID getId() {
        return this.id;
    }

    public Prompts setId(UUID id) {
        this.id = id;
        return this;
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
