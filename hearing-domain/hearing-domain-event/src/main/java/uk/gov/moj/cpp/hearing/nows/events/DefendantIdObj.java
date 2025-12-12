package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class DefendantIdObj implements Serializable {
    private final static long serialVersionUID = -6066870719964371683L;

    private String defendantId;

    @JsonCreator
    public DefendantIdObj(@JsonProperty("defendantId") String defendantId) {
        this.defendantId = defendantId;
    }

    public String getDefendantId() {
        return defendantId;
    }


}
