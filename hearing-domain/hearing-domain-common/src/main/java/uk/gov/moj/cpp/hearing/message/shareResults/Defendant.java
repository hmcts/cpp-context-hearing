package uk.gov.moj.cpp.hearing.message.shareResults;

import java.util.List;
import java.util.UUID;

public class Defendant {

    private UUID id;
    private Person person;

    private String defenceOrganisation;

    private Interpreter interpreter;

    private List<Case> cases;

    public static Defendant defendant() {
        return new Defendant();
    }

    public UUID getId() {
        return id;
    }

    public Defendant setId(UUID id) {
        this.id = id;
        return this;
    }

    public Person getPerson() {
        return person;
    }

    public Defendant setPerson(Person person) {
        this.person = person;
        return this;
    }

    public String getDefenceOrganisation() {
        return defenceOrganisation;
    }

    public Defendant setDefenceOrganisation(String defenceOrganisation) {
        this.defenceOrganisation = defenceOrganisation;
        return this;
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public Defendant setInterpreter(Interpreter interpreter) {
        this.interpreter = interpreter;
        return this;
    }

    public List<Case> getCases() {
        return cases;
    }

    public Defendant setCases(List<Case> cases) {
        this.cases = cases;
        return this;
    }

}
