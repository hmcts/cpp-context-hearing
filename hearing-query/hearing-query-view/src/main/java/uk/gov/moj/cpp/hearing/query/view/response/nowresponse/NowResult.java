
package uk.gov.moj.cpp.hearing.query.view.response.nowresponse;

import com.fasterxml.jackson.annotation.JsonProperty;


public class NowResult {

    @JsonProperty("sharedResultId")
    private String sharedResultId;
    @JsonProperty("sequence")
    private Integer sequence;

    public String getSharedResultId() {
        return sharedResultId;
    }

    public void setSharedResultId(String sharedResultId) {
        this.sharedResultId = sharedResultId;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String sharedResultId;
        private Integer sequence;

        private Builder() {
        }

        public Builder withSharedResultId(String sharedResultId) {
            this.sharedResultId = sharedResultId;
            return this;
        }

        public Builder withSequence(Integer sequence) {
            this.sequence = sequence;
            return this;
        }

        public NowResult build() {
            NowResult material = new NowResult();
            material.setSharedResultId(sharedResultId);
            material.setSequence(sequence);
            return material;
        }
    }
}
