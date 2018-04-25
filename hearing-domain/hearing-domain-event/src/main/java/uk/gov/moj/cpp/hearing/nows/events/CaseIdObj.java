package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;

public class CaseIdObj implements Serializable {

    private final static long serialVersionUID = -946375622900806650L;
    private String caseId;

    public CaseIdObj(String caseId) {
        this.caseId = caseId;
    }

    public String getCaseId() {
        return caseId;
    }
}
