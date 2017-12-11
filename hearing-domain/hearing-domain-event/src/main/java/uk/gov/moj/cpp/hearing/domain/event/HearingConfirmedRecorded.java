package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.external.domain.listing.Hearing;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.hearing.confirmed-recorded")
public class HearingConfirmedRecorded {

    private UUID caseId;
    private String urn;
    private Hearing hearing;

    @JsonCreator
    public HearingConfirmedRecorded(@JsonProperty(value = "caseId") final UUID caseId,
                                    @JsonProperty(value = "urn") final String urn,
                                    @JsonProperty(value = "hearing") final Hearing hearing) {
        this.caseId = caseId;
        this.urn = urn;
        this.hearing = hearing;
    }

    public HearingConfirmedRecorded() {
        // default constructor for Jackson serialisation
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
