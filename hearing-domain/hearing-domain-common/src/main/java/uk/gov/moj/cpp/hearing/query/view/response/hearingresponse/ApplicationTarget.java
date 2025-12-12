package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import java.util.UUID;

public class ApplicationTarget {

    private UUID targetId;
    private UUID applicationId;
    private String draftResult;

    public static ApplicationTarget applicationTarget() {
        return new ApplicationTarget();
    }

    public UUID getTargetId() {
        return targetId;
    }

    public ApplicationTarget setTargetId(final UUID targetId) {
        this.targetId = targetId;
        return this;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public ApplicationTarget setApplicationId(final UUID applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public String getDraftResult() {
        return draftResult;
    }

    public ApplicationTarget setDraftResult(final String draftResult) {
        this.draftResult = draftResult;
        return this;
    }

}