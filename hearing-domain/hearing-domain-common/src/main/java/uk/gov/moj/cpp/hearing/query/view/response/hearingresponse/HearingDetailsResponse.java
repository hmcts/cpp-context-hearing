package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import uk.gov.justice.core.courts.Hearing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.moj.cpp.hearing.domain.HearingState;

import java.util.List;
import java.util.UUID;

@SuppressWarnings({"squid:S2384"})
public class HearingDetailsResponse {

    private Hearing hearing;

    private HearingState hearingState;

    private UUID amendedByUserId;

    private List<String> witnesses;

    public HearingDetailsResponse() {
    }

    @JsonCreator
    public HearingDetailsResponse(
        @JsonProperty("hearing") final Hearing hearing,
        @JsonProperty("hearingState") final HearingState hearingState,
        @JsonProperty("amendedByUserId") final UUID amendedByUserId
    ) {
        this.hearing = hearing;
        this.hearingState = hearingState;
        this.amendedByUserId = amendedByUserId;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public void setHearing(Hearing hearing) {
        this.hearing = hearing;
    }

    public HearingState getHearingState() {
        return hearingState;
    }

    public void setHearingState(HearingState hearingState) {
        this.hearingState = hearingState;
    }

    public UUID getAmendedByUserId() {
        return amendedByUserId;
    }

    public void setAmendedByUserId(UUID amendedByUserId) {
        this.amendedByUserId = amendedByUserId;
    }

    public List<String> getWitnesses() {
        return witnesses;
    }

    public void setWitnesses(List<String> witnesses) {
        this.witnesses = witnesses;
    }
}