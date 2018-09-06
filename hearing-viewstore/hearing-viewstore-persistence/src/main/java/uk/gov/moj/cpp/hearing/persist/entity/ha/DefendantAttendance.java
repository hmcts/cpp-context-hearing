package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.UUID;

// Will be covered by GPE-5565 story
public class DefendantAttendance {

    private UUID hearingId;

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }
}