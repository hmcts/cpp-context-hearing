package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.events.found-witnesses-for-hearing-to-inherit")
public class FoundWitnessesForHearingToInherit {

    private UUID id;
    private UUID hearingId;
    private String type;
    private String classification;
    private String title;
    private String firstName;
    private String lastName;
    private UUID defendantId;

    private FoundWitnessesForHearingToInherit() {
    }

    @JsonCreator
    public FoundWitnessesForHearingToInherit(
            @JsonProperty("id") final UUID id,
            @JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("type") final String type,
            @JsonProperty("classification") final String classification,
            @JsonProperty("title") final String title,
            @JsonProperty("firstName") final String firstName,
            @JsonProperty("lastName") final String lastName,
            @JsonProperty("defendantId") final UUID defendantId) {
        this.hearingId = hearingId;
        this.id = id;
        this.type = type;
        this.classification = classification;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.defendantId = defendantId;
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

    public UUID getDefendantId() {
        return defendantId;
    }

    public FoundWitnessesForHearingToInherit setId(UUID id) {
        this.id = id;
        return this;
    }

    public FoundWitnessesForHearingToInherit setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public FoundWitnessesForHearingToInherit setType(String type) {
        this.type = type;
        return this;
    }

    public FoundWitnessesForHearingToInherit setClassification(String classification) {
        this.classification = classification;
        return this;
    }

    public FoundWitnessesForHearingToInherit setTitle(String title) {
        this.title = title;
        return this;
    }

    public FoundWitnessesForHearingToInherit setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public FoundWitnessesForHearingToInherit setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public FoundWitnessesForHearingToInherit setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public static FoundWitnessesForHearingToInherit foundWitnessesForHearingToInherit() {
        return new FoundWitnessesForHearingToInherit();
    }
}
