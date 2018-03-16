package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.case-created")
public class CaseCreated {

    private UUID caseId;

    private String urn;

    public CaseCreated(){

    }

    public CaseCreated(UUID caseId, String urn) {
        this.caseId = caseId;
        this.urn = urn;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getUrn() {
        return urn;
    }
}
