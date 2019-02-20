package uk.gov.moj.cpp.hearing.domain.event;


import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.hearing-adjourned")
public class HearingAdjourned implements Serializable {

    private static final long serialVersionUID = -6276173236506491225L;

    private final UUID adjournedHearing;
    private final List<NextHearing> nextHearings;

    @JsonCreator
    public HearingAdjourned(@JsonProperty(value = "adjournedHearing") final UUID adjournedHearing, @JsonProperty(value = "nextHearings") final List<NextHearing> nextHearings) {
        this.adjournedHearing = adjournedHearing;
        this.nextHearings = nextHearings;
    }

    public UUID getAdjournedHearing() {
        return adjournedHearing;
    }

    public List<NextHearing> getNextHearings() {
        return nextHearings;
    }
}