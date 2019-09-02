package uk.gov.moj.cpp.hearing.command.application;

import uk.gov.justice.core.courts.CourtApplicationResponse;

import java.io.Serializable;
import java.util.UUID;

public class SaveApplicationResponseCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private UUID applicationPartyId;
    private CourtApplicationResponse applicationResponse;

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }

    public CourtApplicationResponse getApplicationResponse() {
        return applicationResponse;
    }

    public void setApplicationResponse(final CourtApplicationResponse applicationResponse) {
        this.applicationResponse = applicationResponse;
    }

    public UUID getApplicationPartyId() {
        return applicationPartyId;
    }

    public void setApplicationPartyId(final UUID applicationPartyId) {
        this.applicationPartyId = applicationPartyId;
    }
}
