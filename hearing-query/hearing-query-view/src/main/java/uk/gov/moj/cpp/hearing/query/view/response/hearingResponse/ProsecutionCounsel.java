package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "attendeeId",
        "status",
        "title",
        "firstName",
        "lastName"
})
public class ProsecutionCounsel {
    private String attendeeId;
    private String status;
    private String title;
    private String firstName;
    private String lastName;

    public String getAttendeeId() {
        return attendeeId;
    }

    public void setAttendeeId(String attendeeId) {
        this.attendeeId = attendeeId;
    }

    public ProsecutionCounsel withAttendeeId(String attendeeId) {
        this.attendeeId = attendeeId;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ProsecutionCounsel withStatus(String status) {
        this.status = status;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ProsecutionCounsel withTitle(String title) {
        this.title = title;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public ProsecutionCounsel withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public ProsecutionCounsel withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }
}
