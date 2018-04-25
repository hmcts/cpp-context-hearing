package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;


public class DefendantIdObj implements Serializable {
    private final static long serialVersionUID = -6066870719964371683L;

    private String defendantId;

    public DefendantIdObj(String defendantId) {
        this.defendantId = defendantId;
    }

    public String getDefendantId() {
        return defendantId;
    }


}
