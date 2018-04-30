package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;
@SuppressWarnings({"squid:S00107"})
@Event("hearing.defence-witness-added")
public class DefenceWitnessAdded {
    private final UUID witnessId;
    private final UUID defendantId;
    private final UUID hearingId;
    private final String type;
    private final String classification;
    private final String title;
    private final String firstName;
    private final String lastName;

    public DefenceWitnessAdded(final UUID witnessId, final UUID defendantId, final UUID hearingId, final String type, final String classification, final String title, final String firstName, final String lastName) {
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
