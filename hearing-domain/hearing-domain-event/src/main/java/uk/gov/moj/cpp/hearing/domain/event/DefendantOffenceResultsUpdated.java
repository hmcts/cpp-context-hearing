package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.domain.OffenceResult;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.defendant-offence-results-updated")
public class DefendantOffenceResultsUpdated {

    private final UUID defendantId;

    private final List<UUID> offenceIds;

    private final Map<UUID, OffenceResult> resultedOffences;

    @JsonCreator
    @SuppressWarnings("squid:S2384")
    public DefendantOffenceResultsUpdated(
            @JsonProperty("defendantId") final UUID defendantId,
            @JsonProperty("offenceIds") final List<UUID> offenceIds,
            @JsonProperty("resultedOffences") final Map<UUID, OffenceResult> resultedOffences) {
        this.defendantId = defendantId;
        this.offenceIds = offenceIds;
        this.resultedOffences = resultedOffences;
    }

    private DefendantOffenceResultsUpdated(final Builder builder) {
        defendantId = builder.defendantId;
        offenceIds = builder.offenceIds;
        resultedOffences = builder.resultedOffences;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final DefendantOffenceResultsUpdated copy) {
        final Builder builder = new Builder();
        builder.defendantId = copy.getDefendantId();
        builder.offenceIds = copy.getOffenceIds();
        builder.resultedOffences = copy.getResultedOffences();
        return builder;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    @SuppressWarnings("squid:S2384")
    public List<UUID> getOffenceIds() {
        return offenceIds;
    }

    public Map<UUID, OffenceResult> getResultedOffences() {
        return resultedOffences;
    }


    public static final class Builder {
        private UUID defendantId;
        private List<UUID> offenceIds;
        private Map<UUID, OffenceResult> resultedOffences;

        private Builder() {
        }

        public Builder withDefendantId(final UUID val) {
            defendantId = val;
            return this;
        }

        @SuppressWarnings("squid:S2384")
        public Builder withOffenceIds(final List<UUID> val) {
            offenceIds = val;
            return this;
        }

        public Builder withResultedOffences(final Map<UUID, OffenceResult> val) {
            resultedOffences = val;
            return this;
        }

        public DefendantOffenceResultsUpdated build() {
            return new DefendantOffenceResultsUpdated(this);
        }
    }

    @Override
    public String toString() {
        return "DefendantOffenceResultsUpdated{" +
                "defendantId=" + defendantId +
                ", offenceIds=" + offenceIds +
                ", resultedOffences=" + resultedOffences +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DefendantOffenceResultsUpdated that = (DefendantOffenceResultsUpdated) o;
        return Objects.equals(defendantId, that.defendantId) &&
                Objects.equals(offenceIds, that.offenceIds) &&
                Objects.equals(resultedOffences, that.resultedOffences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defendantId, offenceIds, resultedOffences);
    }
}
