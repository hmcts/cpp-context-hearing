package uk.gov.moj.cpp.hearing.command.verdict;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class HearingVerdict {

    private final UUID hearingId;
    private final UUID caseId;
    private final UUID defendantId;
    private final UUID personId;
    private final UUID offenceId;
    private final Verdict verdict;

    @JsonCreator
    public HearingVerdict(@JsonProperty(value = "caseId") final UUID caseId,
                          @JsonProperty(value = "hearingId") final UUID hearingId,
                          @JsonProperty(value = "defendantId") final UUID defendantId,
                          @JsonProperty(value = "personId") final UUID personId,
                          @JsonProperty(value = "offenceId") final UUID offenceId,
                          @JsonProperty(value = "verdict") final Verdict verdict) {
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.offenceId = offenceId;
        this.verdict = verdict;
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

    public Verdict getVerdict() {
        return this.verdict;
    }

    public UUID getPersonId() {
        return this.personId;
    }

    public UUID getHearingId() {
        return this.hearingId;
    }
}
