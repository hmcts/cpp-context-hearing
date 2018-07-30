package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;

public class Attendees implements Serializable {

    private static final long serialVersionUID = 1L;

    private String firstName;

    private String lastName;

    private String type;

    public static Attendees attendees() {
        return new Attendees();
    }

    public String getFirstName() {
        return this.firstName;
    }

    public Attendees setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return this.lastName;
    }

    public Attendees setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public Attendees setType(String type) {
        this.type = type;
        return this;
    }
}
