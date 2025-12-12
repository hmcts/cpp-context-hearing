package uk.gov.moj.cpp.hearing.event;

import uk.gov.moj.cpp.hearing.domain.HearingState;
import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class PublicHearingShareResultsFailed {
    private UUID hearingId;
    private HearingState hearingState;
    private UUID amendedByUserId;

    public static PublicHearingShareResultsFailed publicHearingShareResultsFailed() {
        return new PublicHearingShareResultsFailed();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public PublicHearingShareResultsFailed setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public HearingState getHearingState() {
        return hearingState;
    }

    public PublicHearingShareResultsFailed setHearingState(final HearingState hearingState) {
        this.hearingState = hearingState;
        return this;
    }

    public UUID getAmendedByUserId() {
        return amendedByUserId;
    }

    public PublicHearingShareResultsFailed setAmendedByUserId(final UUID amendedByUserId) {
        this.amendedByUserId = amendedByUserId;
        return this;
    }

}
