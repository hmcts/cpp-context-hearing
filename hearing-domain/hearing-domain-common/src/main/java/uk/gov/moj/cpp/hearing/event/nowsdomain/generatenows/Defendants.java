package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Defendants implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private Person person;

    private String defenceOrganisation;

    private Interpreter interpreter;

    private List<Cases> cases;

    public static Defendants defendants() {
        return new Defendants();
    }

    public UUID getId() {
        return this.id;
    }

    public Defendants setId(UUID id) {
        this.id = id;
        return this;
    }

    public Person getPerson() {
        return this.person;
    }

    public Defendants setPerson(Person person) {
        this.person = person;
        return this;
    }

    public String getDefenceOrganisation() {
        return this.defenceOrganisation;
    }

    public Defendants setDefenceOrganisation(String defenceOrganisation) {
        this.defenceOrganisation = defenceOrganisation;
        return this;
    }

    public Interpreter getInterpreter() {
        return this.interpreter;
    }

    public Defendants setInterpreter(Interpreter interpreter) {
        this.interpreter = interpreter;
        return this;
    }

    public List<Cases> getCases() {
        return this.cases;
    }

    public Defendants setCases(List<Cases> cases) {
        this.cases = new ArrayList<>(cases);
        return this;
    }
}
