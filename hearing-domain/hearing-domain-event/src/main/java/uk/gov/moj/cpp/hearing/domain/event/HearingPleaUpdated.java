package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.hearing-plea-updated")
public class HearingPleaUpdated {
    private UUID caseId;

    public HearingPleaUpdated(final UUID caseId) {
        this.caseId = caseId;
    }

    public HearingPleaUpdated() {
        // default constructor for Jackson serialisation
    }

    public UUID getCaseId() {
        return caseId;
    }

}
