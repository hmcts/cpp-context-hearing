package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.plea.Plea;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.case.plea-added")
public class PleaAdded {

    private UUID hearingId;
    private UUID caseId;
    private UUID defendantId;
    private UUID personId;
    private UUID offenceId;
    private Plea plea;

    @JsonCreator
    public PleaAdded(@JsonProperty(value = "caseId") final UUID caseId,
                     @JsonProperty(value = "hearingId") final UUID hearingId,
                     @JsonProperty(value = "defendantId") final UUID defendantId,
                     @JsonProperty(value = "personId") final UUID personId,
                     @JsonProperty(value = "offenceId") final UUID offenceId,
                     @JsonProperty(value = "plea") final Plea plea) {
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.offenceId = offenceId;
        this.plea = plea;
        this.hearingId = hearingId;
        this.personId = personId;
    }

    public PleaAdded() {
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

    public Plea getPlea() {
        return plea;
    }

    public UUID getPersonId() {
        return personId;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
