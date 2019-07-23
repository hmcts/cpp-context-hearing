package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.core.courts.CourtApplicationOutcomeType;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.application-draft-resulted")
public class ApplicationDraftResulted implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID targetId;
    private UUID hearingId;
    private UUID applicationId;
    private String draftResult;
    private CourtApplicationOutcomeType applicationOutcomeType;
    private LocalDate applicationOutcomeDate;

    public static ApplicationDraftResulted applicationDraftResulted() {
        return new ApplicationDraftResulted();
    }

    public UUID getTargetId() {
        return targetId;
    }

    public ApplicationDraftResulted setTargetId(UUID targetId) {
        this.targetId = targetId;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ApplicationDraftResulted setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public ApplicationDraftResulted setApplicationId(final UUID applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public String getDraftResult() {
        return draftResult;
    }

    public ApplicationDraftResulted setDraftResult(final String draftResult) {
        this.draftResult = draftResult;
        return this;
    }

    public CourtApplicationOutcomeType getApplicationOutcomeType() {
        return applicationOutcomeType;
    }

    public ApplicationDraftResulted setApplicationOutcomeType(final CourtApplicationOutcomeType applicationOutcomeType) {
        this.applicationOutcomeType = applicationOutcomeType;
        return this;
    }

    public LocalDate getApplicationOutcomeDate() {
        return applicationOutcomeDate;
    }

    public ApplicationDraftResulted setApplicationOutcomeDate(final LocalDate applicationOutcomeDate) {
        this.applicationOutcomeDate = applicationOutcomeDate;
        return this;
    }
}