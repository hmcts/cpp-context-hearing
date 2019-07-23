package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;


import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ApplicationTargetListResponse {

    private UUID hearingId;
    private List<ApplicationTarget> targets;

    public static ApplicationTargetListResponse applicationTargetListResponse() {
        return new ApplicationTargetListResponse();
    }

    public List<ApplicationTarget> getTargets() {
        if (targets == null) {
            targets = Collections.emptyList();
        }
        return targets;
    }

    public ApplicationTargetListResponse setTargets(final List<ApplicationTarget> targets) {
        this.targets = targets;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ApplicationTargetListResponse setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

}
