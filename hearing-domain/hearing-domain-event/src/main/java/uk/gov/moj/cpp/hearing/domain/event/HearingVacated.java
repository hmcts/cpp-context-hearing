package uk.gov.moj.cpp.hearing.domain.event;

import java.util.UUID;

import uk.gov.justice.domain.annotation.Event;

@Event("hearing.events.hearing-vacated")
public class HearingVacated {

    private UUID hearingId;

    public HearingVacated(UUID hearingId) {
        super();
        this.hearingId = hearingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

}
