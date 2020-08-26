package uk.gov.moj.cpp.hearing.command.bookprovisional;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProvisionalHearingSlotInfo implements Serializable {

    private UUID courtScheduleId;

    private ZonedDateTime hearingStartTime;

    public ProvisionalHearingSlotInfo() {}

    @JsonCreator
    private ProvisionalHearingSlotInfo(
            @JsonProperty("courtScheduleId") final UUID courtScheduleId,
            @JsonProperty("hearingStartTime") final ZonedDateTime hearingStartTime) {
        this.courtScheduleId = courtScheduleId;
        this.hearingStartTime = hearingStartTime;
    }

    public ProvisionalHearingSlotInfo(final UUID courtScheduleId) {
        this.courtScheduleId = courtScheduleId;
    }

    public static ProvisionalHearingSlotInfo bookProvisionalHearingSlotsCommand() {
        return new ProvisionalHearingSlotInfo();
    }

    public UUID getCourtScheduleId() {
        return courtScheduleId;
    }

    public ProvisionalHearingSlotInfo setCourtScheduleId(final UUID courtScheduleId) {
        this.courtScheduleId = courtScheduleId;
        return this;
    }

    public ZonedDateTime getHearingStartTime() {
        return hearingStartTime;
    }

    public ProvisionalHearingSlotInfo setHearingStartTime(final ZonedDateTime hearingStartTime) {
        this.hearingStartTime = hearingStartTime;
        return this;
    }
}
