package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Attendee implements Serializable {

    private final static long serialVersionUID = -2720773212268846807L;
    private String firstName;
    private String lastName;
    private String type;
    private String title;
    private String status;
    private List<DefendantIdObj> defendants = new ArrayList<DefendantIdObj>();
    private List<CaseIdObj> cases = new ArrayList<CaseIdObj>();

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<DefendantIdObj> getDefendants() {
        return defendants;
    }

    public void setDefendants(List<DefendantIdObj> defendants) {
        this.defendants = defendants;
    }

    public List<CaseIdObj> getCases() {
        return cases;
    }

    public void setCases(List<CaseIdObj> cases) {
        this.cases = cases;
    }

    public static final class Builder {
        private String firstName;
        private String lastName;
        private String type;
        private String title;
        private String status;
        private List<DefendantIdObj> defendants = new ArrayList<DefendantIdObj>();
        private List<CaseIdObj> cases = new ArrayList<CaseIdObj>();


        public Builder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withDefendants(List<DefendantIdObj> defendants) {
            this.defendants = defendants;
            return this;
        }

        public Builder withCases(List<CaseIdObj> cases) {
            this.cases = cases;
            return this;
        }

        public Attendee build() {
            Attendee attendee = new Attendee();
            attendee.setFirstName(firstName);
            attendee.setLastName(lastName);
            attendee.setType(type);
            attendee.setTitle(title);
            attendee.setStatus(status);
            attendee.setDefendants(defendants);
            attendee.setCases(cases);
            return attendee;
        }
    }
}
