package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.Verdict;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.events.found-verdict-for-hearing-to-inherit")
public class FoundVerdictForHearingToInherit implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;

    private final Verdict verdict;

    @JsonCreator
    public FoundVerdictForHearingToInherit(@JsonProperty("hearingId") final UUID hearingId,
                                           @JsonProperty("verdict") final Verdict verdict) {
        this.hearingId = hearingId;
        this.verdict = verdict;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public Verdict getVerdict() {
        return this.verdict;
    }
}
