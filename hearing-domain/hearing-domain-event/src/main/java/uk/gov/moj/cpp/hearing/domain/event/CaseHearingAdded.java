package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.case-hearing-added")
public class CaseHearingAdded {

    private UUID caseId;
    private UUID hearingId;

    public CaseHearingAdded(){

    }

    public CaseHearingAdded(UUID caseId, UUID hearingId) {
        this.caseId = caseId;
        this.hearingId = hearingId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
