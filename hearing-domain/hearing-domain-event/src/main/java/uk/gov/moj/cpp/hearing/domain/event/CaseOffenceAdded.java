package uk.gov.moj.cpp.hearing.domain.event;


import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.case-offence-added")
public class CaseOffenceAdded {

    private UUID offenceId;

    private UUID caseId;

    public CaseOffenceAdded(){

    }

    public CaseOffenceAdded(UUID offenceId, UUID caseId) {
        this.offenceId = offenceId;
        this.caseId = caseId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getCaseId() {
        return caseId;
    }
}
