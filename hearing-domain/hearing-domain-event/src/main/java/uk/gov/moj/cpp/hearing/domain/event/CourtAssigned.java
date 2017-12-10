package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.court-assigned")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourtAssigned {

    private final UUID hearingId;
    private final UUID courtCentreId;
    private final String courtCentreName;

    @JsonCreator
    public CourtAssigned(@JsonProperty("hearingId")final UUID hearingId,
                            @JsonProperty("courtCentreName")final String courtCentreName) {
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
