package uk.gov.moj.cpp.external.domain.progression.relist;


import java.io.Serializable;

public class Offence implements Serializable {
    private static final long serialVersionUID = -3734514854083054415L;
    private final String id;

    public Offence(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
