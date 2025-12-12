package uk.gov.moj.cpp.hearing.query.view.response.nowresponse;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class NowsResponse {

    @JsonProperty("nows")
    private List<Nows> nows = new ArrayList<>();

    public static Builder builder() {
        return new Builder();
    }

    public List<Nows> getNows() {
        return nows;
    }

    public void setNows(List<Nows> nows) {
        this.nows = nows;
    }

    public static final class Builder {
        private List<Nows> nows = new ArrayList<>();

        private Builder() {
        }

        public Builder withNows(List<Nows> nows) {
            this.nows = nows;
            return this;
        }

        public NowsResponse build() {
            NowsResponse nowsResponse = new NowsResponse();
            nowsResponse.setNows(nows);
            return nowsResponse;
        }
    }
}
