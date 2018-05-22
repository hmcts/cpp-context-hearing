package uk.gov.moj.cpp.hearing.nows.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class CaseIdObj implements Serializable {

    private final static long serialVersionUID = -946375622900806650L;
    private String caseId;

    @JsonCreator
    public CaseIdObj(@JsonProperty("caseId") String caseId) {
        this.caseId = caseId;
    }

    public String getCaseId() {
        return caseId;
    }
}
