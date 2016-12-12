package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.court-assigned")
public class CourtAssigned {

    private UUID hearingId;

    private String courtCentreName;

    public CourtAssigned(UUID hearingId, String courtCentreName) {
        this.hearingId = hearingId;
        this.courtCentreName = courtCentreName;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }
}
