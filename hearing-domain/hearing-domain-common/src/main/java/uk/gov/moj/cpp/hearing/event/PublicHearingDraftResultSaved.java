package uk.gov.moj.cpp.hearing.event;


import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class PublicHearingDraftResultSaved {
    private UUID defendantId;
    private UUID hearingId;
    private UUID targetId;
    private UUID offenceId;

    public static PublicHearingDraftResultSaved publicHearingDraftResultSaved() {
        return new PublicHearingDraftResultSaved();
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public PublicHearingDraftResultSaved setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public PublicHearingDraftResultSaved setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public PublicHearingDraftResultSaved setTargetId(UUID targetId) {
        this.targetId = targetId;
        return this;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public PublicHearingDraftResultSaved setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }
}