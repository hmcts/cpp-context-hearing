package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import uk.gov.justice.core.courts.Target;

import java.util.Collections;
import java.util.List;

public class TargetListResponse {

    private List<Target> targets;

    public TargetListResponse() {
        this.targets = Collections.emptyList();
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(final List<Target> targets) {
        this.targets = targets;
    }

    public static final class Builder {

        private List<Target> targets;

        public Builder withTargets(final List<Target> targets) {
            this.targets = targets;
            return this;
        }

        public TargetListResponse build() {
            final TargetListResponse targetListResponse = new TargetListResponse();
            targetListResponse.setTargets(targets);
            return targetListResponse;
        }
    }
}
