package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class Attendees {

    private java.util.UUID attendeeId;

    private String firstName;

    private String lastName;

    private String type;

    private String status;

    private String title;

    private java.util.List<DefendantRef> defendants;

    private java.util.List<Cases> cases;

    public static Attendees attendees() {
        return new Attendees();
    }

    public java.util.UUID getAttendeeId() {
        return this.attendeeId;
    }

    public Attendees setAttendeeId(java.util.UUID attendeeId) {
        this.attendeeId = attendeeId;
        return this;
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

    public String getStatus() {
        return this.status;
    }

    public Attendees setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getTitle() {
        return this.title;
    }

    public Attendees setTitle(String title) {
        this.title = title;
        return this;
    }

    public java.util.List<DefendantRef> getDefendants() {
        return this.defendants;
    }

    public Attendees setDefendants(java.util.List<DefendantRef> defendants) {
        this.defendants = defendants;
        return this;
    }

    public java.util.List<Cases> getCases() {
        return this.cases;
    }

    public Attendees setCases(java.util.List<Cases> cases) {
        this.cases = cases;
        return this;
    }
}
