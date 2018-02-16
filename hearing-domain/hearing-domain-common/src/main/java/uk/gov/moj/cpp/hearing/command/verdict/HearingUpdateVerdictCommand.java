package uk.gov.moj.cpp.hearing.command.verdict;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jchondig on 26/01/2018.
 */
public class HearingUpdateVerdictCommand implements Serializable {

    private final UUID hearingId;
    private final UUID caseId;
    private final List<Defendant> defendants;

    @JsonCreator
    public HearingUpdateVerdictCommand(@JsonProperty("hearingId") final UUID hearingId,
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
