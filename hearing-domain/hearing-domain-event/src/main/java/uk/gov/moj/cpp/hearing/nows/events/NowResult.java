package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;
import java.util.List;


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
}
