package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;


public class NowResult implements Serializable {

    private final static long serialVersionUID = 6146686845103889010L;
    private String sharedResultId;
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
}
