package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.hearing-updated-hearing-day-bdf")
@SuppressWarnings({"squid:S2384", "pmd:BeanMembersShouldSerialize"})
public class HearingUpdatedHearingDayBdf implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private HearingDay hearingDay;

    @JsonCreator
    public HearingUpdatedHearingDayBdf(@JsonProperty("hearingId") final UUID hearingId,
                                       @JsonProperty("hearingDay") final HearingDay hearingDay) {
        this.hearingId = hearingId;
        this.hearingDay = hearingDay;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public HearingDay getHearingDay() {
        return hearingDay;
    }
}
