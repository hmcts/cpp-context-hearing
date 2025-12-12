package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.domain.HearingState;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.amended")
public class HearingAmended implements Serializable {


    private UUID hearingId;
    private UUID userId;
    private HearingState newHearingState;

    @JsonCreator
    public HearingAmended(@JsonProperty("hearingId") final UUID hearingId,
                           @JsonProperty("userId") final UUID userId,
                           @JsonProperty("newHearingState") final HearingState newHearingState) {
        this.hearingId = hearingId;
        this.userId = userId;
        this.newHearingState = newHearingState;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(final UUID userId) {
        this.userId = userId;
    }

    public HearingState getNewHearingState() {
        return newHearingState;
    }

    public void setNewHearingState(final HearingState newHearingState) {
        this.newHearingState = newHearingState;
    }
}