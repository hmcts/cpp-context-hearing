package uk.gov.moj.cpp.hearing.domain.command;

import java.time.ZonedDateTime;
import java.util.UUID;

public class EndHearing {

    private UUID hearingId;

    private ZonedDateTime endTime;


    public EndHearing(UUID hearingId, ZonedDateTime endTime) {
        super();
        this.hearingId = hearingId;
        this.endTime = endTime;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }


    @Override
    public String toString() {
        return "InitiateHearing{" +
                "hearingId=" + hearingId +
                ", endTime=" + endTime +
                '}';
    }
}
