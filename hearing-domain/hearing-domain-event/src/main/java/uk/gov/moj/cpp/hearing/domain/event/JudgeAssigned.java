package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@Event("hearing.judge-assigned")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JudgeAssigned {

    private UUID hearingId;
    private String id;
    private String title;
    private String firstName;
    private String lastName;

    public JudgeAssigned(UUID hearingId, String id, String title, String firstName, String lastName) {
        this.hearingId = hearingId;
        this.id = id;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public JudgeAssigned() {
        // default constructor for Jackson serialisation
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
