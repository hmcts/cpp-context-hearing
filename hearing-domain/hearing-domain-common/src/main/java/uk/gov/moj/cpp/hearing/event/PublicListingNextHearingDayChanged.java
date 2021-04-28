package uk.gov.moj.cpp.hearing.event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

public class PublicListingNextHearingDayChanged implements Serializable {

    private static final long serialVersionUID = -6424027187871802879L;

    private final UUID hearingId;

    private final UUID seedingHearingId;

    private final ZonedDateTime hearingStartDate;

    public PublicListingNextHearingDayChanged(final UUID hearingId, final UUID seedingHearingId, ZonedDateTime hearingStartDate) {
        this.hearingId = hearingId;
        this.hearingStartDate = hearingStartDate;
        this.seedingHearingId = seedingHearingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getSeedingHearingId() {
        return seedingHearingId;
    }

    public ZonedDateTime geHearingStartDate() {
        return hearingStartDate;
    }
}
