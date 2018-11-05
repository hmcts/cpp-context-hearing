package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public class LookupPleaOnOffenceForHearingCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID offenceId;
    private UUID caseId;
    private UUID defendantId;
    private UUID hearingId;

    public LookupPleaOnOffenceForHearingCommand(){

    }

    @JsonCreator
    public LookupPleaOnOffenceForHearingCommand(@JsonProperty("offenceId") UUID offenceId,
                                                @JsonProperty("caseId") UUID caseId,
                                                @JsonProperty("defendantId") UUID defendantId,
                                                @JsonProperty("hearingId") UUID hearingId) {
        this.offenceId = offenceId;
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.hearingId = hearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public LookupPleaOnOffenceForHearingCommand setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public LookupPleaOnOffenceForHearingCommand setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public LookupPleaOnOffenceForHearingCommand setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public LookupPleaOnOffenceForHearingCommand setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public static LookupPleaOnOffenceForHearingCommand lookupPleaOnOffenceForHearingCommand(){
        return new LookupPleaOnOffenceForHearingCommand();
    }
}
