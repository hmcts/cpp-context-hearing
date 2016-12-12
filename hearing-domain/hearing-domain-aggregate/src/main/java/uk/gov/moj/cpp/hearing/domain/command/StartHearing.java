package uk.gov.moj.cpp.hearing.domain.command;

import java.time.ZonedDateTime;
import java.util.UUID;

public class StartHearing {

    private UUID hearingId;

    private ZonedDateTime startTime;


    public StartHearing(UUID hearingId, ZonedDateTime startTime) {
        super();
        this.hearingId = hearingId;
        this.startTime = startTime;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }


    @Override
    public String toString() {
        return "InitiateHearing{" +
                "hearingId=" + hearingId +
                ", startTime=" + startTime +
                '}';
    }
}
