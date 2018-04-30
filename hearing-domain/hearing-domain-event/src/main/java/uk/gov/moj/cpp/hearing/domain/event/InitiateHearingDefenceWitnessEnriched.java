package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

@SuppressWarnings({"squid:S00107"})
@Event("hearing.initiate-hearing-defence-witness-enriched")
public class InitiateHearingDefenceWitnessEnriched {

    private String hearingId;
    private String id;
    private String type;
    private String classification;
    private String title;
    private String firstName;
    private String lastName;
    private String defendantId;


    public InitiateHearingDefenceWitnessEnriched(final String id, final String hearingId,
                    final String type,
                    final String classification, final String title, final String firstName,
                    final String lastName, final String defendantId) {
        this.hearingId = hearingId;
        this.id = id;
        this.type = type;
        this.classification = classification;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.defendantId = defendantId;
    }

    public InitiateHearingDefenceWitnessEnriched() {
        // default constructor for Jackson serialisation
    }

    public String getId() {
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

    public String getHearingId() {
        return hearingId;
    }

    public String getDefendantId() {
        return defendantId;
    }
}
