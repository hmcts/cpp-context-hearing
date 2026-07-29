package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import uk.gov.moj.cpp.hearing.domain.OffenceBailStatus;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("squid:S2384")
public class OffenceBailStatusResponse {

    private List<OffenceBailStatus> offenceBailStatuses;

    public OffenceBailStatusResponse() {
        this.offenceBailStatuses = Collections.emptyList();
    }

    public static OffenceBailStatusResponse.Builder builder() {
        return new OffenceBailStatusResponse.Builder();
    }

    public List<OffenceBailStatus> getOffenceBailStatuses() {
        return offenceBailStatuses;
    }

    public void setOffenceBailStatuses(final List<OffenceBailStatus> offenceBailStatuses) {
        this.offenceBailStatuses = offenceBailStatuses;
    }

    public static final class Builder {
        private List<OffenceBailStatus> offenceBailStatuses;

        public OffenceBailStatusResponse.Builder withOffenceBailStatuses(final List<OffenceBailStatus> offenceBailStatuses) {
            this.offenceBailStatuses = offenceBailStatuses;
            return this;
        }

        public OffenceBailStatusResponse build() {
            final OffenceBailStatusResponse response = new OffenceBailStatusResponse();
            response.setOffenceBailStatuses(offenceBailStatuses);
            return response;
        }
    }
}
