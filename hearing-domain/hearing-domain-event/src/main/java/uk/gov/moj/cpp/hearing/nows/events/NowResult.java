package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("squid:S2384")
public class NowResult implements Serializable {

    private final static long serialVersionUID = 6146686845103889010L;
    private String sharedResultId;
    private Integer sequence;
    private List<Prompt> prompts;

    public List<Prompt> getPrompts() {
        return prompts;
    }

    public void setPrompts(List<Prompt> prompts) {
        this.prompts = prompts;
    }

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
        private List<Prompt> prompts;

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
