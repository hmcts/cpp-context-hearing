package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Event("hearing.events.hearing-deleted")
@SuppressWarnings({"squid:S2384", "pmd:BeanMembersShouldSerialize"})
public class HearingDeleted implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<UUID> prosecutionCaseIds;

    private List<UUID> defendantIds;

    private List<UUID> offenceIds;

    private List<UUID> courtApplicationIds;

    private UUID hearingId;

    @JsonCreator
    public HearingDeleted(@JsonProperty("prosecutionCaseIds") final List<UUID> prosecutionCaseIds,
                          @JsonProperty("defendantIds") final List<UUID> defendantIds,
                          @JsonProperty("offenceIds") final List<UUID> offenceIds,
                          @JsonProperty("courtApplicationIds") final List<UUID> courtApplicationIds,
                          @JsonProperty("hearingId") final UUID hearingId) {
        this.prosecutionCaseIds = prosecutionCaseIds;
        this.defendantIds = defendantIds;
        this.offenceIds = offenceIds;
        this.courtApplicationIds = courtApplicationIds;
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

    public List<UUID> getCourtApplicationIds() {
        return courtApplicationIds;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
