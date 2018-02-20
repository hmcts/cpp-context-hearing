package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.draft-result-saved")
public class DraftResultSaved {
    private UUID targetId;
    private UUID defendantId;
    private UUID offenceId;
    private String draftResult;
    private UUID hearingId;

    public DraftResultSaved(
            final UUID targetId,
            final UUID defendantId,
            final UUID offenceId,
            final String draftResult,
            final UUID hearingId
    ) {
        this.targetId = targetId;
        this.defendantId = defendantId;
        this.offenceId = offenceId;
        this.draftResult = draftResult;
        this.hearingId = hearingId;
    }

    public DraftResultSaved() {
        // default constructor for Jackson serialisation
    }

    public UUID getTargetId() {
        return targetId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public String getDraftResult() {
        return draftResult;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
