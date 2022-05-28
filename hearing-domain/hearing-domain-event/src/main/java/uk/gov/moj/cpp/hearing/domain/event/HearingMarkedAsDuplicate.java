package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("squid:S2384")
@Event("hearing.events.marked-as-duplicate")
public class HearingMarkedAsDuplicate implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<UUID> prosecutionCaseIds;

    private List<UUID> defendantIds;

    private List<UUID> offenceIds;

    private UUID hearingId;

    private UUID courtCentreId;

    @JsonCreator
    public HearingMarkedAsDuplicate(@JsonProperty("prosecutionCaseIds") final List<UUID> prosecutionCaseIds, @JsonProperty("defendantIds") final List<UUID> defendantIds, @JsonProperty("offenceIds") final List<UUID> offenceIds, @JsonProperty("hearingId") final UUID hearingId, @JsonProperty("courtCentreId") final UUID courtCentreId) {
        this.prosecutionCaseIds = prosecutionCaseIds;
        this.defendantIds = defendantIds;
        this.offenceIds = offenceIds;
        this.hearingId = hearingId;
        this.courtCentreId = courtCentreId;
    }

    public List<UUID> getProsecutionCaseIds() {
        return prosecutionCaseIds;
    }

    public void setProsecutionCaseIds(final List<UUID> prosecutionCaseIds) {
        this.prosecutionCaseIds = prosecutionCaseIds;
    }

    public List<UUID> getDefendantIds() {
        return defendantIds;
    }

    public void setDefendantIds(final List<UUID> defendantIds) {
        this.defendantIds = defendantIds;
    }

    public List<UUID> getOffenceIds() {
        return offenceIds;
    }

    public void setOffenceIds(final List<UUID> offenceIds) {
        this.offenceIds = offenceIds;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public void setCourtCentreId(final UUID courtCentreId) {
        this.courtCentreId = courtCentreId;
    }
}
