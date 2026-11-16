package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.events.case-registered-for-extended-hearing")
public class CaseRegisteredForExtendedHearing implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID caseId;
    private final UUID hearingId;

    public CaseRegisteredForExtendedHearing(final UUID caseId, final UUID hearingId) {
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
