package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Event("hearing.events.earliest-next-hearing-date-changed")
public class EarliestNextHearingDateChanged implements Serializable {

    private static final long serialVersionUID = 1816408864871472452L;

    private final UUID hearingId;

    private final UUID seedingHearingId;

    private final ZonedDateTime earliestNextHearingDate;

    @JsonCreator
    public EarliestNextHearingDateChanged(@JsonProperty("hearingId") final UUID hearingId,
                                          @JsonProperty("seedingHearingId") final UUID seedingHearingId,
                                          @JsonProperty("earliestNextHearingDate") final ZonedDateTime earliestNextHearingDate) {

        this.hearingId = hearingId;
        this.seedingHearingId = seedingHearingId;
        this.earliestNextHearingDate = earliestNextHearingDate;

    }

    public UUID getHearingId() {
        return this.hearingId;
    }

    public UUID getSeedingHearingId() {
        return this.seedingHearingId;
    }

    public ZonedDateTime getEarliestNextHearingDate() {
        return this.earliestNextHearingDate;
    }

}
