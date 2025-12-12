package uk.gov.moj.cpp.hearing.command.initiate;

import uk.gov.justice.core.courts.Verdict;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateHearingWithInheritedVerdictCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private Verdict verdict;

    public UpdateHearingWithInheritedVerdictCommand() {
    }

    @JsonCreator
    public UpdateHearingWithInheritedVerdictCommand(@JsonProperty("hearingId") final UUID hearingId,
                                                    @JsonProperty("verdict") final Verdict verdict) {
        this.hearingId = hearingId;
        this.verdict = verdict;
    }

    public static UpdateHearingWithInheritedVerdictCommand updateHearingWithInheritedVerdictCommand() {
        return new UpdateHearingWithInheritedVerdictCommand();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UpdateHearingWithInheritedVerdictCommand setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public UpdateHearingWithInheritedVerdictCommand setVerdict(final Verdict verdict) {
        this.verdict = verdict;
        return this;
    }
}