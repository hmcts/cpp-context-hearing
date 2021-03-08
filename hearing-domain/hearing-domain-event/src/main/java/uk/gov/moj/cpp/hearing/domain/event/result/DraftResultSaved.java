package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.Target;
import uk.gov.moj.cpp.hearing.domain.HearingState;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
@Event("hearing.draft-result-saved")
public class DraftResultSaved implements Serializable {

    private static final long serialVersionUID = 1L;

    private Target target;

    private HearingState hearingState;

    private UUID amendedByUserId;

    @JsonCreator
    public DraftResultSaved(
            @JsonProperty("target") final Target target,
            @JsonProperty("hearingState") final HearingState hearingState,
            @JsonProperty("amendedByUserId") final UUID amendedByUserId
    ) {
        this.target = target;
        this.hearingState = hearingState;
        this.amendedByUserId = amendedByUserId;
    }

    public void setHearingState(final HearingState hearingState) {
        this.hearingState = hearingState;
    }

    public void setAmendedByUserId(final UUID amendedByUserId) {
        this.amendedByUserId = amendedByUserId;
    }


    public Target getTarget() {
        return target;
    }

    public HearingState getHearingState() {
        return hearingState;
    }

    public UUID getAmendedByUserId() {
        return amendedByUserId;
    }
}