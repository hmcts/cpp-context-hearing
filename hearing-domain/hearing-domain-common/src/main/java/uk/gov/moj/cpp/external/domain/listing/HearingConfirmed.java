package uk.gov.moj.cpp.external.domain.listing;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = Include.NON_NULL)
public class HearingConfirmed implements Serializable {

    private final UUID caseId;
    private final String urn;
    private final Hearing hearing;

    @JsonCreator
    public HearingConfirmed(@JsonProperty(value = "caseId") final UUID caseId,
                            @JsonProperty(value = "urn") final String urn,
                            @JsonProperty(value = "hearing") final Hearing hearing
    ) {
        this.caseId = caseId;
        this.urn = urn;
        this.hearing = hearing;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getUrn() {
        return urn;
    }

    public Hearing getHearing() {
        return hearing;
    }
}
