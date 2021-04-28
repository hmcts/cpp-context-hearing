package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.domain.annotation.Event;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Event("hearing.events.existing-hearing-updated")
public class ExistingHearingUpdated implements Serializable {

    private static final long serialVersionUID = -7451539865499565268L;
    private final UUID hearingId;
    private final List<ProsecutionCase> prosecutionCases;
    private List<UUID> shadowListedOffences;

    @JsonCreator
    public ExistingHearingUpdated(@JsonProperty("hearingId") final UUID hearingId,
                                  @JsonProperty("prosecutionCases") final List<ProsecutionCase> prosecutionCases,
                                  @JsonProperty("shadowListedOffences") final List<UUID> shadowListedOffences) {
        this.hearingId = hearingId;
        this.prosecutionCases = prosecutionCases;
        this.shadowListedOffences = shadowListedOffences;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<ProsecutionCase> getProsecutionCases() {
        return prosecutionCases;
    }

    public List<UUID> getShadowListedOffences() {
        return shadowListedOffences;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.hearingId, this.prosecutionCases, this.shadowListedOffences);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ExistingHearingUpdated that = (ExistingHearingUpdated) o;
        return Objects.equals(this.hearingId, that.hearingId)
                && Objects.equals(this.prosecutionCases, that.prosecutionCases)
                && Objects.equals(this.shadowListedOffences, that.shadowListedOffences);
    }
}
