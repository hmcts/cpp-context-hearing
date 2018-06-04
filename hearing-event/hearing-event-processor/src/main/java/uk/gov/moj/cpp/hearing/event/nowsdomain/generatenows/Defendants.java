package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class Defendants {

    private java.util.UUID id;

    private Person person;

    private String bailStatus;

    private String defenceOrganisation;

    private Interpreter interpreter;

    private java.util.List<Cases> cases;

    public static Defendants defendants() {
        return new Defendants();
    }

    public java.util.UUID getId() {
        return this.id;
    }

    public Defendants setId(java.util.UUID id) {
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

    public String getBailStatus() {
        return this.bailStatus;
    }

    public Defendants setBailStatus(String bailStatus) {
        this.bailStatus = bailStatus;
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

    public java.util.List<Cases> getCases() {
        return this.cases;
    }

    public Defendants setCases(java.util.List<Cases> cases) {
        this.cases = cases;
        return this;
    }
}
