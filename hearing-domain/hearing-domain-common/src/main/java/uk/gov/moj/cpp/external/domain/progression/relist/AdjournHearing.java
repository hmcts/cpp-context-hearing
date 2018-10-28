package uk.gov.moj.cpp.external.domain.progression.relist;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

@JsonInclude(value = Include.NON_NULL)
public class AdjournHearing {

    private UUID adjournedHearing;
    private List<Hearing> nextHearings;

    public AdjournHearing() {
    }

    @JsonCreator
    public AdjournHearing(@JsonProperty(value = "adjournedHearing") final UUID adjournedHearing, @JsonProperty(value = "nextHearings") final List<Hearing> nextHearings) {
        this.adjournedHearing = adjournedHearing;
        this.nextHearings = nextHearings;
    }

    public UUID getAdjournedHearing() {
        return adjournedHearing;
    }

    public AdjournHearing setAdjournedHearing(UUID adjournedHearing) {
        this.adjournedHearing = adjournedHearing;
        return this;
    }

    public List<Hearing> getNextHearings() {
        return nextHearings;
    }

    public AdjournHearing setNextHearings(List<Hearing> nextHearings) {
        this.nextHearings = nextHearings;
        return this;
    }

    public static AdjournHearing adjournHearing() {
        return new AdjournHearing();
    }
}
