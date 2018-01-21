package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@Event("hearing.court-assigned")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourtAssigned {

    private UUID hearingId;
    private UUID courtCentreId;
    private String courtCentreName;

    public CourtAssigned(final UUID hearingId, final UUID courtCentreId, final String courtCentreName) {
        this.hearingId = hearingId;
        this.courtCentreId = courtCentreId;
        this.courtCentreName = courtCentreName;
    }

    public CourtAssigned() {
        // default constructor for Jackson serialisation
    }

    public CourtAssigned(UUID hearingId, String courtCentreName) {
        this.hearingId = hearingId;
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
