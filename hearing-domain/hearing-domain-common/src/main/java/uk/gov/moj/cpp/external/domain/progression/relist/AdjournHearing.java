package uk.gov.moj.cpp.external.domain.progression.relist;

import uk.gov.justice.core.courts.NextHearing;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = Include.NON_NULL)
public class AdjournHearing {

    private UUID adjournedHearing;
    private List<NextHearing> nextHearings;

    public AdjournHearing() {
    }

    @JsonCreator
    public AdjournHearing(@JsonProperty(value = "adjournedHearing") final UUID adjournedHearing, @JsonProperty(value = "nextHearings") final List<NextHearing> nextHearings) {
        this.adjournedHearing = adjournedHearing;
        this.nextHearings = nextHearings;
    }

    public static AdjournHearing adjournHearing() {
        return new AdjournHearing();
    }

    public UUID getAdjournedHearing() {
        return adjournedHearing;
    }

    public AdjournHearing setAdjournedHearing(UUID adjournedHearing) {
        this.adjournedHearing = adjournedHearing;
        return this;
    }

    public List<NextHearing> getNextHearings() {
        return nextHearings;
    }

    public AdjournHearing setNextHearings(List<NextHearing> nextHearings) {
        this.nextHearings = nextHearings;
        return this;
    }
}
