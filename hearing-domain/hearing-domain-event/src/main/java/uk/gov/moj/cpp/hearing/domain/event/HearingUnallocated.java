package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.hearing-unallocated")
@SuppressWarnings({"squid:S2384", "pmd:BeanMembersShouldSerialize"})
public class HearingUnallocated implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<UUID> prosecutionCaseIds;

    private List<UUID> defendantIds;

    private List<UUID> offenceIds;

    private UUID hearingId;

    @JsonCreator
    public HearingUnallocated(@JsonProperty("prosecutionCaseIds") final List<UUID> prosecutionCaseIds,
                              @JsonProperty("defendantIds") final List<UUID> defendantIds,
                              @JsonProperty("offenceIds") final List<UUID> offenceIds,
                              @JsonProperty("hearingId") final UUID hearingId) {
        this.prosecutionCaseIds = prosecutionCaseIds;
        this.defendantIds = defendantIds;
        this.offenceIds = offenceIds;
        this.hearingId = hearingId;
    }

    public List<UUID> getProsecutionCaseIds() {
        return prosecutionCaseIds;
    }

    public List<UUID> getDefendantIds() {
        return defendantIds;
    }

    public List<UUID> getOffenceIds() {
        return offenceIds;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
