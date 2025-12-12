package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.defence-witness-added")
public class DefenceWitnessAdded implements Serializable {

    private static final long serialVersionUID = 1L;
    private final UUID witnessId;
    private final UUID defendantId;
    private final UUID hearingId;
    private final String type;
    private final String classification;
    private final String title;
    private final String firstName;
    private final String lastName;

    @JsonCreator
    public DefenceWitnessAdded(@JsonProperty("witnessId") final UUID witnessId,
                               @JsonProperty("defendantId") final UUID defendantId,
                               @JsonProperty("hearingId") final UUID hearingId,
                               @JsonProperty("type") final String type,
                               @JsonProperty("classification") final String classification,
                               @JsonProperty("title") final String title,
                               @JsonProperty("firstName") final String firstName,
                               @JsonProperty("lastName") final String lastName) {
        this.witnessId = witnessId;
        this.defendantId = defendantId;
        this.hearingId = hearingId;
        this.type = type;
        this.classification = classification;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getWitnessId() {
        return witnessId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getHearingId() {
        return hearingId;
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
}
