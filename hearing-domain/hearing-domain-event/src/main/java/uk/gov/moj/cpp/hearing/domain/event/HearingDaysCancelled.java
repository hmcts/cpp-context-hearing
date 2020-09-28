package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.hearing-days-cancelled")
public class HearingDaysCancelled implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;
    private final List<HearingDay> hearingDays;

    @JsonCreator
    public HearingDaysCancelled(@JsonProperty("hearingId") final UUID hearingId,
                                @JsonProperty("hearingDays") final List<HearingDay> hearingDays) {
        this.hearingId = hearingId;
        this.hearingDays = new ArrayList<>(hearingDays);
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<HearingDay> getHearingDays() {
        return new ArrayList<>(hearingDays);
    }
}
