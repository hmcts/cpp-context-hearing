package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.court-assigned")
public class CourtAssigned {

    private final UUID hearingId;
    private final UUID courtCentreId;
    private final String courtCentreName;

    public CourtAssigned(final UUID hearingId, final String courtCentreName) {
        this.hearingId = hearingId;
        this.courtCentreName = courtCentreName;
        this.courtCentreId = null;
    }

    public CourtAssigned(final UUID hearingId, final UUID courtCentreId, final String courtCentreName) {
        this.hearingId = hearingId;
        this.courtCentreId = courtCentreId;
        this.courtCentreName = courtCentreName;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }
}
