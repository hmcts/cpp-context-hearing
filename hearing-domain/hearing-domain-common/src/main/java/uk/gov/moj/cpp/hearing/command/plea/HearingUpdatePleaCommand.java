package uk.gov.moj.cpp.hearing.command.plea;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class HearingUpdatePleaCommand implements Serializable {
    final private UUID hearingId;
    final private UUID caseId;
    final private List<Defendant> defendants;

    @JsonCreator
    public HearingUpdatePleaCommand(@JsonProperty("hearingId") final UUID hearingId,
                                    @JsonProperty("caseId") final UUID caseId,
                                    @JsonProperty("defendants") final List<Defendant> defendants) {
        this.caseId = caseId;
        this.hearingId = hearingId;
        this.defendants = (null == defendants) ? new ArrayList<>() : new ArrayList<>(defendants);

    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }
}
