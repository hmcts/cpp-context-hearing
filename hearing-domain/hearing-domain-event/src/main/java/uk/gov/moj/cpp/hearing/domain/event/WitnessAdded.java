package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.DefendantId;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"squid:S00107"})
@Event("hearing.events.witness-added")
public class WitnessAdded implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private UUID id;
    private String type;
    private String classification;
    private String title;
    private String firstName;
    private String lastName;
    private List<UUID> defendantIds;

    @JsonCreator
    public WitnessAdded(@JsonProperty("id") UUID id,
                        @JsonProperty("hearingId") UUID hearingId,
                        @JsonProperty("type") String type,
                        @JsonProperty("classification") String classification,
                        @JsonProperty("title") String title,
                        @JsonProperty("firstName") String firstName,
                        @JsonProperty("lastName") String lastName,
                        @JsonProperty("defendantIds") List<UUID> defendantIds) {
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
