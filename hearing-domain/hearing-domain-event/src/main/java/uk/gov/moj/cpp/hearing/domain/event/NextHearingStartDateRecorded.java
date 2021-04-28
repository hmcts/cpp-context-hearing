package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Event("hearing.events.next-hearing-start-date-recorded")
public class NextHearingStartDateRecorded implements Serializable {

    private static final long serialVersionUID = -6411311742347593990L;

    private final UUID hearingId;

    private final UUID seedingHearingId;

    private final ZonedDateTime nextHearingStartDate;

    @JsonCreator
    public NextHearingStartDateRecorded(@JsonProperty("hearingId") final UUID hearingId,
                                        @JsonProperty("seedingHearingId") final UUID seedingHearingId,
                                        @JsonProperty("nextHearingStartDate") final ZonedDateTime nextHearingStartDate) {

        this.hearingId = hearingId;
        this.seedingHearingId = seedingHearingId;
        this.nextHearingStartDate = nextHearingStartDate;

    }

    public UUID getHearingId() {
        return this.hearingId;
    }

    public UUID getSeedingHearingId() {
        return this.seedingHearingId;
    }

    public ZonedDateTime getNextHearingStartDate() {
        return this.nextHearingStartDate;
    }

}
