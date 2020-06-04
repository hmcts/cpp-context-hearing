package uk.gov.moj.cpp.hearing.domain.event;


import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.vacated-trial-cleared")
public class HearingEventVacatedTrialCleared implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;

    @JsonCreator
    public HearingEventVacatedTrialCleared(@JsonProperty("hearingId") UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}