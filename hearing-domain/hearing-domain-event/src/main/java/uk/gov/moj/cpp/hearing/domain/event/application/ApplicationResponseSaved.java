package uk.gov.moj.cpp.hearing.domain.event.application;

import uk.gov.justice.core.courts.CourtApplicationResponse;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.application-response-saved")
public class ApplicationResponseSaved implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID applicationPartyId;

    private CourtApplicationResponse courtApplicationResponse;

    public static ApplicationResponseSaved applicationResponseSaved() {
        return new ApplicationResponseSaved();
    }

    public UUID getApplicationPartyId() {
        return applicationPartyId;
    }

    public ApplicationResponseSaved setApplicationPartyId(final UUID applicationPartyId) {
        this.applicationPartyId = applicationPartyId;
        return this;
    }

    public CourtApplicationResponse getCourtApplicationResponse() {
        return courtApplicationResponse;
    }

    public ApplicationResponseSaved setCourtApplicationResponse(final CourtApplicationResponse courtApplicationResponse) {
        this.courtApplicationResponse = courtApplicationResponse;
        return this;
    }
}