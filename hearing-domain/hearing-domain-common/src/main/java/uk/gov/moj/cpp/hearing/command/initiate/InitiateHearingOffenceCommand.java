package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class InitiateHearingOffenceCommand {

    private UUID offenceId;
    private UUID caseId;
    private UUID defendantId;
    private UUID hearingId;

    @JsonCreator
    public InitiateHearingOffenceCommand(@JsonProperty("offenceId") UUID offenceId,
                                         @JsonProperty("caseId") UUID caseId,
                                         @JsonProperty("defendantId") UUID defendantId,
                                         @JsonProperty("hearingId") UUID hearingId) {
        this.offenceId = offenceId;
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.hearingId = hearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

}
