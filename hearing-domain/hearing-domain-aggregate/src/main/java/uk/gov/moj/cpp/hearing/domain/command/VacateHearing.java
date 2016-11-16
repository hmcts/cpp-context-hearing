package uk.gov.moj.cpp.hearing.domain.command;

import java.util.UUID;

public class VacateHearing {

    private UUID hearingId;

    public VacateHearing(UUID hearingId) {
        super();
        this.hearingId = hearingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

}
