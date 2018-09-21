package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.Verdict;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.offence-verdict-updated")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerdictUpsert implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private Verdict verdict;

    public VerdictUpsert() {
    }

    @JsonCreator
    protected VerdictUpsert(@JsonProperty("hearingId") final UUID hearingId,
                            @JsonProperty("verdict") final Verdict verdict) {
        this.hearingId = hearingId;
        this.verdict = verdict;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public VerdictUpsert setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public VerdictUpsert setVerdict(final Verdict verdict) {
        this.verdict = verdict;
        return this;
    }

    public Verdict getVerdict() {
        return this.verdict;
    }

    public static VerdictUpsert verdictUpsert() {
        return new VerdictUpsert();
    }

}
