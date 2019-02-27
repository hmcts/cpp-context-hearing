package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HearingListResponse {

    private final List<HearingListResponseHearing> hearingListResponseHearings;

    public HearingListResponse() {
        this.hearingListResponseHearings = Collections.emptyList();
    }

    @JsonCreator
    public HearingListResponse(@JsonProperty("hearings") final List<HearingListResponseHearing> hearingListResponseHearings) {
        this.hearingListResponseHearings = hearingListResponseHearings;
    }

    private HearingListResponse(final Builder builder) {
        this.hearingListResponseHearings = builder.hearingListResponseHearings;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<HearingListResponseHearing> getHearings() {
        return hearingListResponseHearings;
    }

    public static final class Builder {

        private List<HearingListResponseHearing> hearingListResponseHearings;

        public HearingListResponse.Builder withHearings(final List<HearingListResponseHearing> hearingListResponseHearings) {
            this.hearingListResponseHearings = hearingListResponseHearings;
            return this;
        }

        public HearingListResponse build() {
            return new HearingListResponse(this);
        }
    }
}