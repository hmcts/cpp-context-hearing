package uk.gov.moj.cpp.hearing.command.verdict;

import uk.gov.justice.json.schemas.core.Verdict;

import java.util.UUID;

public class UpdateOffenceVerdictCommand {

    private UUID hearingId;

    private Verdict verdict;

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public void setVerdict(Verdict verdict) {
        this.verdict = verdict;
    }
}
