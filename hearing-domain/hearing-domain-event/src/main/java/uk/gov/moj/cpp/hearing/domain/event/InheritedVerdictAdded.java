package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.Verdict;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.events.inherited-verdict-added")
public class InheritedVerdictAdded implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private Verdict verdict;

    @JsonCreator
    public InheritedVerdictAdded(@JsonProperty("hearingId") final UUID hearingId,
                                 @JsonProperty("verdict") final Verdict verdict) {
        this.hearingId = hearingId;
        this.verdict = verdict;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public Verdict getVerdict() {
        return verdict;
    }
}
