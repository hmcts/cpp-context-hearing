package uk.gov.moj.cpp.hearing.domain.command;


import java.util.UUID;

public class AddCaseToHearing {
    private UUID hearingId;
    private UUID caseId;

    public AddCaseToHearing(UUID hearingId, UUID caseId) {
        this.hearingId = hearingId;
        this.caseId = caseId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getCaseId() {
        return caseId;
    }
}
