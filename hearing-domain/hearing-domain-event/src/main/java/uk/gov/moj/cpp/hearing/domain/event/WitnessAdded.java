package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.List;
import java.util.UUID;

@SuppressWarnings({"squid:S00107"})
@Event("hearing.events.witness-added")
public class WitnessAdded {

    private UUID hearingId;
    private UUID id;
    private String type;
    private String classification;
    private String title;
    private String firstName;
    private String lastName;
    private List<UUID> defendantIds;


    public WitnessAdded(UUID id, UUID hearingId, String type, String classification, String title, String firstName, String lastName,  List<UUID> defendantIds) {
        this.hearingId = hearingId;
        this.id = id;
        this.type = type;
        this.classification = classification;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.defendantIds = defendantIds;
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

    public List<UUID> getDefendantIds() {
        return defendantIds;
    }
}
