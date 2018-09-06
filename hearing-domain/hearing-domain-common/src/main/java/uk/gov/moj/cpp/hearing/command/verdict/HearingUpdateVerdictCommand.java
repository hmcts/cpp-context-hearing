package uk.gov.moj.cpp.hearing.command.verdict;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class HearingUpdateVerdictCommand implements Serializable {

    private static final long serialVersionUID = 1L;
    private UUID hearingId;
    private List<Verdict> verdicts;

    public HearingUpdateVerdictCommand() {
    }

    @JsonCreator
    protected HearingUpdateVerdictCommand(@JsonProperty("hearingId") final UUID hearingId,
                                          @JsonProperty("verdicts") final List<Verdict> verdicts) {
        this.hearingId = hearingId;
        this.verdicts = verdicts;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<Verdict> getVerdicts() {
        return verdicts;
    }

    public HearingUpdateVerdictCommand withHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public HearingUpdateVerdictCommand withVerdicts(List<Verdict> verdicts) {
        this.verdicts = verdicts;
        return this;
    }

    public static HearingUpdateVerdictCommand hearingUpdateVerdictCommand(){
        return new HearingUpdateVerdictCommand();
    }
}