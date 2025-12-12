package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.UUID;

//Will be covered by GGPE-5825 story
public class ProsecutionCounsel {

    private UUID hearingId;

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }
}
