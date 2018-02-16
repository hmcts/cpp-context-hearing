package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.hearing-verdict-updated")
public class HearingVerdictUpdated {
    private UUID hearingId;

    public HearingVerdictUpdated(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public HearingVerdictUpdated() {
        // default constructor for Jackson serialisation
    }

    public UUID getHearingId() {
        return hearingId;
    }

}
