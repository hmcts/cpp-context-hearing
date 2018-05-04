package uk.gov.moj.cpp.hearing.event.nows.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NowsOrderResult implements Serializable {

    private final static long serialVersionUID = 5323524335441343266L;
    private String label;
    private List<NowOrderPrompt> prompts = new ArrayList<NowOrderPrompt>();

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<NowOrderPrompt> getPrompts() {
        return prompts;
    }

    public void setPrompts(List<NowOrderPrompt> prompts) {
        this.prompts = prompts;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String label;
        private List<NowOrderPrompt> prompts = new ArrayList<NowOrderPrompt>();

        private Builder() {
        }

        public Builder withLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder withPrompts(List<NowOrderPrompt> prompts) {
            this.prompts = prompts;
            return this;
        }

        public NowsOrderResult build() {
            NowsOrderResult nowsOrderResult = new NowsOrderResult();
            nowsOrderResult.setLabel(label);
            nowsOrderResult.setPrompts(prompts);
            return nowsOrderResult;
        }
    }
}
