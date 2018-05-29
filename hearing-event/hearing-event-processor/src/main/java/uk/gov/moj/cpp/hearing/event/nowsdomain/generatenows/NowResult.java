package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class NowResult {

    private java.util.UUID sharedResultId;

    private Integer sequence;

    private java.util.List<PromptRefs> promptRefs;

    public static NowResult nowResult() {
        return new NowResult();
    }

    public java.util.UUID getSharedResultId() {
        return this.sharedResultId;
    }

    public NowResult setSharedResultId(java.util.UUID sharedResultId) {
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

    public java.util.List<PromptRefs> getPromptRefs() {
        return this.promptRefs;
    }

    public NowResult setPromptRefs(java.util.List<PromptRefs> promptRefs) {
        this.promptRefs = promptRefs;
        return this;
    }
}
