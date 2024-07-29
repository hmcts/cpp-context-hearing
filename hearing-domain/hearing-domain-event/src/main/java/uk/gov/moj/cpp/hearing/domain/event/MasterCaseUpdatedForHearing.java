package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.events.master-case-updated-for-hearing")
public class MasterCaseUpdatedForHearing implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID caseId;
    private final UUID hearingId;

    public MasterCaseUpdatedForHearing(final UUID caseId, final UUID hearingId) {
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
