package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.offence-created")
public class OffenceCreated {
    private UUID offenceId;
    private UUID caseId;
    private UUID defendantId;

    public OffenceCreated(UUID offenceId, UUID caseId, UUID defendantId) {
        this.offenceId = offenceId;
        this.caseId = caseId;
        this.defendantId = defendantId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }
}
