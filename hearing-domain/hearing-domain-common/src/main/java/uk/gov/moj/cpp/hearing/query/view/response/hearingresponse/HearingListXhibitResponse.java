package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import java.util.UUID;

public class HearingListXhibitResponse {

    private UUID courtCentreId;

    public HearingListXhibitResponse(final UUID courtCentreId) {
        this.courtCentreId = courtCentreId;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }
}
