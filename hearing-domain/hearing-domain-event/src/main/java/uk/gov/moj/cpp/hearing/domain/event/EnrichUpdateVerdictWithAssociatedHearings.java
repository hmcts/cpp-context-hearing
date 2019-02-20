package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.Verdict;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.enrich-update-verdict-with-associated-hearings")
public class EnrichUpdateVerdictWithAssociatedHearings implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Verdict verdict;

    private final List<UUID> hearingIds;

    @JsonCreator
    public EnrichUpdateVerdictWithAssociatedHearings(@JsonProperty("hearingIds") final List<UUID> hearingIds,
                                                     @JsonProperty("verdict") final Verdict verdict) {
        this.verdict = verdict;
        this.hearingIds = new ArrayList<>(hearingIds);
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    @Override
    public String toString() {
        return "EnrichUpdateVerdictWithAssociatedHearings{" +
                "verdict=" + verdict +
                ", hearingIds=" + hearingIds +
                '}';
    }
}