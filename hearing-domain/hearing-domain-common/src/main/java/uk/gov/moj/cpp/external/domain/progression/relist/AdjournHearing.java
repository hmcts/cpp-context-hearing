package uk.gov.moj.cpp.external.domain.progression.relist;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = Include.NON_NULL)
public class AdjournHearing {

    private final UUID caseId;
    private final String urn;
    private final UUID requestedByHearingId;
    private final List<Hearing> hearings;

    @JsonCreator
    public AdjournHearing(@JsonProperty(value = "caseId") final UUID caseId,
                          @JsonProperty(value = "urn") final String urn,
                          @JsonProperty(value = "requestedByHearingId") final UUID requestedByHearingId,
                          @JsonProperty(value = "hearings") final List<Hearing> hearings
    ) {
        this.caseId = caseId;
        this.urn = urn;
        this.requestedByHearingId = requestedByHearingId;
        this.hearings = hearings;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getUrn() {
        return urn;
    }

    public List<Hearing> getHearings() {
        return hearings;
    }

    public UUID getRequestedByHearingId() {
        return requestedByHearingId;
    }

}
