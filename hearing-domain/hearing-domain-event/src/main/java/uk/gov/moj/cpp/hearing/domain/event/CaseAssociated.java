package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.case-associated")
public class CaseAssociated {

    private UUID hearingId;

    private UUID caseId;

    public CaseAssociated(UUID hearingId, UUID caseId) {
        this.hearingId = hearingId;
        this.caseId = caseId;
    }

    public CaseAssociated() {
        // default constructor for Jackson serialisation
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getCaseId() {
        return caseId;
    }
}
