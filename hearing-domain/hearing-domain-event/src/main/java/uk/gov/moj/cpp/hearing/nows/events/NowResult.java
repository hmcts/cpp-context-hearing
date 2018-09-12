package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class NowResult implements Serializable {

    private final static long serialVersionUID = 2L;
    private UUID sharedResultId;
    private Integer sequence;
    private List<Prompt> prompts;

    public List<Prompt> getPrompts() {
        return prompts;
    }

    public NowResult setPrompts(List<Prompt> prompts) {
        this.prompts = prompts;
        return this;
    }

    public UUID getSharedResultId() {
        return sharedResultId;
    }

    public NowResult setSharedResultId(UUID sharedResultId) {
        this.sharedResultId = sharedResultId;
        return this;
    }

    public Integer getSequence() {
        return sequence;
    }

    public NowResult setSequence(Integer sequence) {
        this.sequence = sequence;
        return this;
    }

    public static NowResult nowResult(){
        return new NowResult();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID sharedResultId;
        private Integer sequence;
        private List<Prompt> prompts;

        private Builder() {
        }

        public Builder withSharedResultId(UUID sharedResultId) {
            this.sharedResultId = sharedResultId;
            return this;
        }

        public Builder withSequence(Integer sequence) {
            this.sequence = sequence;
            return this;
        }

        public Builder withPrompts(List<Prompt> prompts) {
            this.prompts = prompts;
            return this;
        }

        public NowResult build() {
            final NowResult nowResult = new NowResult();
            nowResult.setSharedResultId(sharedResultId);
            nowResult.setSequence(sequence);
            nowResult.setPrompts(prompts);
            return nowResult;
        }
    }
}
