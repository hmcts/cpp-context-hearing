package uk.gov.moj.cpp.hearing.domain.event;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.external.domain.progression.relist.Hearing;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Event("hearing.event.hearing-adjourned")
public class HearingAdjourned implements Serializable {

    private static final long serialVersionUID = -6276173236506491225L;

    private final UUID adjournedHearing;
    private final List<Hearing> nextHearings;

    @JsonCreator
    public HearingAdjourned(@JsonProperty(value = "adjournedHearing") final UUID adjournedHearing, @JsonProperty(value = "nextHearings") final List<Hearing> nextHearings) {
        this.adjournedHearing = adjournedHearing;
        this.nextHearings = nextHearings;
    }

    public UUID getAdjournedHearing() {
        return adjournedHearing;
    }

    public List<Hearing> getNextHearings() {
        return nextHearings;
    }
}