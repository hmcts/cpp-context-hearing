package uk.gov.moj.cpp.hearing.event;


import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class PublicHearingApplicationDraftResulted {
    private UUID applicationId;
    private UUID hearingId;
    private UUID targetId;

    public static PublicHearingApplicationDraftResulted publicHearingApplicationDraftResulted() {
        return new PublicHearingApplicationDraftResulted();
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public PublicHearingApplicationDraftResulted setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public PublicHearingApplicationDraftResulted setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public PublicHearingApplicationDraftResulted setTargetId(UUID targetId) {
        this.targetId = targetId;
        return this;
    }

}
