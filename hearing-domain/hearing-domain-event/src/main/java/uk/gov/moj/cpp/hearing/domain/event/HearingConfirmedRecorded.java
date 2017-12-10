package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.external.domain.listing.Hearing;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.confirmed-recorded")
public class HearingConfirmedRecorded {

    private final UUID caseId;
    private final String urn;
    private final Hearing hearing;

    @JsonCreator
    public HearingConfirmedRecorded(@JsonProperty(value = "caseId") final UUID caseId,
                                    @JsonProperty(value = "urn") final String urn,
                                    @JsonProperty(value = "hearing") final Hearing hearing) {
        this.caseId = caseId;
        this.urn = urn;
        this.hearing = hearing;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getUrn() {
        return urn;
    }

    public Hearing getHearing() {
        return hearing;
    }
}
