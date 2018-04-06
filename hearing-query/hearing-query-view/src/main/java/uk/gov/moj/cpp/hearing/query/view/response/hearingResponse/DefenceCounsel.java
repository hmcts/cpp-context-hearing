package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "attendeeId",
        "status",
        "defendantId",
        "title",
        "firstName",
        "lastName"
})
public class DefenceCounsel {
    private String attendeeId;
    private String status;
    private String defendantId;
    private String title;
    private String firstName;
    private String lastName;

    public String getAttendeeId() {
        return attendeeId;
    }

    public void setAttendeeId(String attendeeId) {
        this.attendeeId = attendeeId;
    }

    public DefenceCounsel withAttendeeId(String attendeeId) {
        this.attendeeId = attendeeId;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DefenceCounsel withStatus(String status) {
        this.status = status;
        return this;
    }

    public String getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(String defendantId) {
        this.defendantId = defendantId;
    }

    public DefenceCounsel withDefendantId(String defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DefenceCounsel withTitle(String title) {
        this.title = title;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public DefenceCounsel withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public DefenceCounsel withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }
}
