package uk.gov.moj.cpp.hearing.command.plea;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HearingPlea {

    private final UUID hearingId;
    private final UUID caseId;
    private final UUID defendantId;
    private final UUID personId;
    private final UUID offenceId;
    private final Plea plea;

    @JsonCreator
    public HearingPlea(@JsonProperty(value = "caseId") final UUID caseId,
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

    public UUID getCaseId() {
        return this.caseId;
    }

    public UUID getDefendantId() {
        return this.defendantId;
    }

    public UUID getOffenceId() {
        return this.offenceId;
    }

    public Plea getPlea() {
        return this.plea;
    }

    public UUID getPersonId() {
        return this.personId;
    }

    public UUID getHearingId() {
        return this.hearingId;
    }
}
