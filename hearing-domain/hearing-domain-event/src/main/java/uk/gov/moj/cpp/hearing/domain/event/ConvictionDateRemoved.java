package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.conviction-date-removed")
public class ConvictionDateRemoved {

    private UUID hearingId;
    private UUID caseId;
    private UUID defendantId;
    private UUID personId;
    private UUID offenceId;

    @JsonCreator
    public ConvictionDateRemoved(@JsonProperty(value = "caseId") final UUID caseId,
                                 @JsonProperty(value = "hearingId") final UUID hearingId,
                                 @JsonProperty(value = "defendantId") final UUID defendantId,
                                 @JsonProperty(value = "personId") final UUID personId,
                                 @JsonProperty(value = "offenceId") final UUID offenceId) {
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.offenceId = offenceId;
        this.hearingId = hearingId;
        this.personId = personId;
    }

    public ConvictionDateRemoved() {
        // default constructor for Jackson serialisation
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getPersonId() {
        return personId;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
