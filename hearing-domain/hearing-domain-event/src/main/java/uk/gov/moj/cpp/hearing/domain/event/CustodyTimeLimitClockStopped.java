package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.custody-time-limit-clock-stopped")
@SuppressWarnings({"squid:S2384", "pmd:BeanMembersShouldSerialize"})
public class CustodyTimeLimitClockStopped implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private List<UUID> offenceIds;

    @JsonCreator
    public CustodyTimeLimitClockStopped(@JsonProperty("hearingId") final UUID hearingId,
                                        @JsonProperty("offenceIds") final List<UUID> offenceIds
    ) {
        this.hearingId = hearingId;
        this.offenceIds = offenceIds;

    }

    public List<UUID> getOffenceIds() {
        return offenceIds;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
