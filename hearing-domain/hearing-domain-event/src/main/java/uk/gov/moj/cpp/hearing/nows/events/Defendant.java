package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Defendant implements Serializable {

    private final static long serialVersionUID = 4811811680624099822L;
    private String id;
    private Person person;
    private String defenceOrganisation;
    private Interpreter interpreter;
    private List<Case> cases = new ArrayList<Case>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getDefenceOrganisation() {
        return defenceOrganisation;
    }

    public void setDefenceOrganisation(String defenceOrganisation) {
        this.defenceOrganisation = defenceOrganisation;
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public void setInterpreter(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public List<Case> getCases() {
        return cases;
    }

    public void setCases(List<Case> cases) {
        this.cases = cases;
    }

    public static final class Builder {
        private String id;
        private Person person;
        private String defenceOrganisation;
        private Interpreter interpreter;
        private List<Case> cases = new ArrayList<Case>();


        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withPerson(Person person) {
            this.person = person;
            return this;
        }

        public Builder withDefenceOrganisation(String defenceOrganisation) {
            this.defenceOrganisation = defenceOrganisation;
            return this;
        }

        public Builder withInterpreter(Interpreter interpreter) {
            this.interpreter = interpreter;
            return this;
        }

        public Builder withCases(List<Case> cases) {
            this.cases = cases;
            return this;
        }

        public Defendant build() {
            Defendant defendant = new Defendant();
            defendant.setId(id);
            defendant.setPerson(person);
            defendant.setDefenceOrganisation(defenceOrganisation);
            defendant.setInterpreter(interpreter);
            defendant.setCases(cases);
            return defendant;
        }
    }
}
