package uk.gov.moj.cpp.hearing.event;


import uk.gov.justice.core.courts.CourtApplicationOutcomeType;

import java.time.LocalDate;
import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class PublicHearingApplicationDraftResulted {
    private UUID applicationId;
    private UUID hearingId;
    private UUID targetId;
    private CourtApplicationOutcomeType applicationOutcomeType;
    private LocalDate applicationOutcomeDate;

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

    public CourtApplicationOutcomeType getApplicationOutcomeType() {
        return applicationOutcomeType;
    }

    public PublicHearingApplicationDraftResulted setApplicationOutcomeType(final CourtApplicationOutcomeType applicationOutcomeType) {
        this.applicationOutcomeType = applicationOutcomeType;
        return this;
    }

    public LocalDate getApplicationOutcomeDate() {
        return applicationOutcomeDate;
    }

    public PublicHearingApplicationDraftResulted setApplicationOutcomeDate(final LocalDate applicationOutcomeDate) {
        this.applicationOutcomeDate = applicationOutcomeDate;
        return this;
    }
}
