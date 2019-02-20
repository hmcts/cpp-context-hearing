package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.Verdict;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.inherited-verdict-added")
public class InheritedVerdictAdded implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private Verdict verdict;

    public InheritedVerdictAdded() {
    }

    @JsonCreator
    public InheritedVerdictAdded(@JsonProperty("hearingId") final UUID hearingId,
                                 @JsonProperty("verdict") final Verdict verdict) {
        this.hearingId = hearingId;
        this.verdict = verdict;
    }

    public InheritedVerdictAdded setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this ;
    }

    public InheritedVerdictAdded setVerdict(final Verdict verdict) {
        this.verdict = verdict;
        return this ;
    }
    public UUID getHearingId() {
        return hearingId;
    }

    public Verdict getVerdict() {
        return verdict;
    }
}
