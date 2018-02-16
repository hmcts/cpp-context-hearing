package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;

import java.util.UUID;

@Event("hearing.verdict-added")
public class VerdictAdded {


    private UUID hearingId;
    private UUID caseId;
    private UUID defendantId;
    private UUID personId;
    private UUID offenceId;
    private Verdict verdict;

    @JsonCreator
    public VerdictAdded(@JsonProperty(value = "caseId") final UUID caseId,
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

    public VerdictAdded() {
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

    public Verdict getVerdict() {
        return verdict;
    }

    public UUID getPersonId() {
        return personId;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
