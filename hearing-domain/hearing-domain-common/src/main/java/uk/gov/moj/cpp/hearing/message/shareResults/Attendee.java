package uk.gov.moj.cpp.hearing.message.shareResults;

public class Attendee<T extends Attendee> {

    private String firstName;

    private String lastName;

    private String type;

    private String title;


    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public T setFirstName(String firstName) {
        this.firstName = firstName;
        return (T) this;
    }

    public T setLastName(String lastName) {
        this.lastName = lastName;
        return (T) this;
    }

    public T setType(String type) {
        this.type = type;
        return (T) this;
    }

    public T setTitle(String title) {
        this.title = title;
        return (T) this;
    }

    public static <T extends Attendee> Attendee<T> attendee(){
        return new Attendee<>();
    }
}
