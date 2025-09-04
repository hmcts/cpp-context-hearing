package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import uk.gov.justice.hearing.courts.HearingView;
import uk.gov.moj.cpp.hearing.domain.HearingState;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384"})
public class HearingViewResponse {

    private HearingView hearing;

    private HearingState hearingState;

    private UUID amendedByUserId;

    private List<String> witnesses;

    private ZonedDateTime firstSharedDate;


    public HearingViewResponse() {
    }

    @JsonCreator
    public HearingViewResponse(
            @JsonProperty("hearing") final HearingView hearing,
            @JsonProperty("hearingState") final HearingState hearingState,
            @JsonProperty("amendedByUserId") final UUID amendedByUserId
    ) {
        this.hearing = hearing;
        this.hearingState = hearingState;
        this.amendedByUserId = amendedByUserId;
    }

    public HearingView getHearing() {
        return hearing;
    }

    public void setHearing(HearingView hearing) {
        this.hearing = hearing;
    }


    public List<String> getWitnesses() {
        return witnesses;
    }

    public void setWitnesses(List<String> witnesses) {
        this.witnesses = witnesses;
    }

    public ZonedDateTime getFirstSharedDate() {
        return firstSharedDate;
    }

    public void setFirstSharedDate(ZonedDateTime firstSharedDate) {
        this.firstSharedDate = firstSharedDate;
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

}