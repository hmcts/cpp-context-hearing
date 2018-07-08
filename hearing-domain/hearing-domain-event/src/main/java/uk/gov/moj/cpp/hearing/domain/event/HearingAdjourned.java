package uk.gov.moj.cpp.hearing.domain.event;


import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.external.domain.progression.relist.Hearing;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.hearing-adjourned")
public class HearingAdjourned implements Serializable {

    private static final long serialVersionUID = -6276173236506491225L;
    private final UUID caseId;
    private final String urn;
    private final List<Hearing> hearings;

    @JsonCreator
    public HearingAdjourned(@JsonProperty(value = "caseId") final UUID caseId,
                            @JsonProperty(value = "urn") final String urn,
                            @JsonProperty(value = "hearings") final List<Hearing> hearings) {
        this.caseId = caseId;
        this.urn = urn;
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
}
