package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Event("hearing.events.offences-removed-from-existing-hearing")
public class OffencesRemovedFromExistingHearing implements Serializable {

    private static final long serialVersionUID = -4529051147739803041L;

    private UUID hearingId;

    private List<UUID> prosecutionCaseIds;

    private List<UUID> defendantIds;

    private List<UUID> offenceIds;

    private String sourceContext;

    @JsonCreator
    public OffencesRemovedFromExistingHearing(@JsonProperty("hearingId") final UUID hearingId,
                                              @JsonProperty("prosecutionCaseIds") final List<UUID> prosecutionCaseIds,
                                              @JsonProperty("defendantIds") final List<UUID> defendantIds,
                                              @JsonProperty("offenceIds") final List<UUID> offenceIds,
                                              @JsonProperty("sourceContext") final String sourceContext) {

        this.hearingId = hearingId;
        this.prosecutionCaseIds = prosecutionCaseIds;
        this.defendantIds = defendantIds;
        this.offenceIds = offenceIds;
        this.sourceContext = sourceContext;
    }

    public UUID getHearingId() {
        return hearingId;
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

    public String getSourceContext() {
        return sourceContext;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.hearingId, this.prosecutionCaseIds, this.defendantIds, this.offenceIds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OffencesRemovedFromExistingHearing that = (OffencesRemovedFromExistingHearing) o;
        return Objects.equals(this.hearingId, that.hearingId)
                && Objects.equals(this.prosecutionCaseIds, that.prosecutionCaseIds)
                && Objects.equals(this.defendantIds, that.defendantIds)
                && Objects.equals(this.offenceIds, that.offenceIds);
    }

}
