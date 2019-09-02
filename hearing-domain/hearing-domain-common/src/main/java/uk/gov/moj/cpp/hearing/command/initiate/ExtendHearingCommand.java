package uk.gov.moj.cpp.hearing.command.initiate;

import uk.gov.justice.core.courts.CourtApplication;

import java.io.Serializable;
import java.util.UUID;

public class ExtendHearingCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private CourtApplication courtApplication;

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }

    public CourtApplication getCourtApplication() {
        return courtApplication;
    }

    public void setCourtApplication(CourtApplication courtApplication) {
        this.courtApplication = courtApplication;
    }
}
