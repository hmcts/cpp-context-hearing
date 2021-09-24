package uk.gov.moj.cpp.hearing.event;


import uk.gov.moj.cpp.hearing.domain.HearingState;

import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class PublicHearingAmended {

    private UUID hearingId;
    private HearingState newHearingState;


    public static PublicHearingAmended publicHearingAmended() {
        return new PublicHearingAmended();
    }



    public PublicHearingAmended setNewHearingState(HearingState newHearingState) {
        this.newHearingState = newHearingState;
        return this;
    }

    public UUID getHearingId() {
        return this.hearingId;
    }

    public PublicHearingAmended setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public HearingState getNewHearingState() {
        return this.newHearingState;
    }


}