package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class NowResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID sharedResultId;

    private Integer sequence;

    private List<PromptRef> prompts;

    public static NowResult nowResult() {
        return new NowResult();
    }

    public UUID getSharedResultId() {
        return this.sharedResultId;
    }

    public NowResult setSharedResultId(UUID sharedResultId) {
        this.sharedResultId = sharedResultId;
        return this;
    }

    public Integer getSequence() {
        return this.sequence;
    }

    public NowResult setSequence(Integer sequence) {
        this.sequence = sequence;
        return this;
    }

    public List<PromptRef> getPrompts() {
        return this.prompts;
    }

    public NowResult setPrompts(List<PromptRef> prompts) {
        this.prompts = prompts;
        return this;
    }
}
