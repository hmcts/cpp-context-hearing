package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NowListResponse {

    private final List<NowResponse> nows;

    public NowListResponse() {
        this.nows = Collections.emptyList();
    }

    @JsonCreator
    public NowListResponse(@JsonProperty("nows") final List<NowResponse> nows) {
        this.nows = nows;
    }

    private NowListResponse(final Builder builder) {
        this.nows = builder.nows;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<NowResponse> getNows() {
        return nows;
    }

    public static final class Builder {

        private List<NowResponse> nows;

        public NowListResponse.Builder withNows(final List<NowResponse> nows) {
            this.nows = nows;
            return this;
        }

        public NowListResponse build() {
            return new NowListResponse(this);
        }
    }
}