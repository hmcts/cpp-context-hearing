package uk.gov.moj.cpp.hearing.command.hearing.details;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

public class RecordNextHearingDayUpdatedCommand implements Serializable {

    private static final long serialVersionUID = -6424027187871802879L;

    private final UUID hearingId;

    private final UUID seedingHearingId;

    private final ZonedDateTime hearingStartDate;

    public RecordNextHearingDayUpdatedCommand(final UUID hearingId, final UUID seedingHearingId, final ZonedDateTime hearingStartDate) {
        this.hearingId = hearingId;
        this.seedingHearingId = seedingHearingId;
        this.hearingStartDate = hearingStartDate;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getSeedingHearingId() {
        return seedingHearingId;
    }

    public ZonedDateTime getHearingStartDate() {
        return hearingStartDate;
    }

}
