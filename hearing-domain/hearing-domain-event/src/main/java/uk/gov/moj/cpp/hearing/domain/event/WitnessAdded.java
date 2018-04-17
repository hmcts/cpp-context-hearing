package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.events.witness-added")
public class WitnessAdded {

    private UUID hearingId;
    private UUID id;
    private UUID caseId;
    private String type;
    private String classification;
    private UUID personId;
    private String title;
    private String firstName;
    private String lastName;


    public WitnessAdded(UUID id, UUID hearingId, UUID caseId, String type, String classification, UUID personId, String title, String firstName, String lastName) {
        this.hearingId = hearingId;
        this.id = id;
        this.caseId = caseId;
        this.type = type;
        this.classification = classification;
        this.personId = personId;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public WitnessAdded() {
        // default constructor for Jackson serialisation
    }

    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getClassification() {
        return classification;
    }

    public UUID getPersonId() {
        return personId;
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

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getCaseId() {
        return caseId;
    }
}
