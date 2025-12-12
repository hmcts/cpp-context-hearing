package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.application-detail-changed")
public class ApplicationDetailChanged implements Serializable {

    private static final long serialVersionUID = 2L;

    private final UUID hearingId;
    private final CourtApplication courtApplication;

    @JsonCreator
    public ApplicationDetailChanged(@JsonProperty("hearingId") final UUID hearingId, @JsonProperty("courtApplication") final CourtApplication courtApplication) {
        this.hearingId = hearingId;
        this.courtApplication = courtApplication;
    }

    public CourtApplication getCourtApplication() {
        return courtApplication;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}