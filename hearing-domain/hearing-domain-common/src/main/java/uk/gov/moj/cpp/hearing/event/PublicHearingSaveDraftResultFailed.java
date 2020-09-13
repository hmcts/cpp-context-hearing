package uk.gov.moj.cpp.hearing.event;


import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class PublicHearingSaveDraftResultFailed {
    private UUID defendantId;
    private UUID hearingId;
    private UUID targetId;
    private UUID offenceId;
    private String draftResult;

    public static PublicHearingSaveDraftResultFailed publicHearingSaveDraftResultFailed() {
        return new PublicHearingSaveDraftResultFailed();
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public PublicHearingSaveDraftResultFailed setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public PublicHearingSaveDraftResultFailed setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public PublicHearingSaveDraftResultFailed setTargetId(UUID targetId) {
        this.targetId = targetId;
        return this;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public PublicHearingSaveDraftResultFailed setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public String getDraftResult() {
        return draftResult;
    }

    public PublicHearingSaveDraftResultFailed setDraftResult(String draftResult) {
        this.draftResult = draftResult;
        return this;
    }
}
