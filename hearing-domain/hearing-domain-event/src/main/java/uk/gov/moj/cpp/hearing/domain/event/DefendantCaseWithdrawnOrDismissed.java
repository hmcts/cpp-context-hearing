package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.domain.OffenceResult;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.defendant-case-withdrawn-or-dismissed")
public class DefendantCaseWithdrawnOrDismissed {

    private final UUID caseId;
    private final UUID defendantId;
    private final Map<UUID, OffenceResult> resultedOffences;

    @JsonCreator
    public DefendantCaseWithdrawnOrDismissed(
            @JsonProperty("caseId") final UUID caseId,
            @JsonProperty("defendantId") final UUID defendantId,
            @JsonProperty("resultedOffences") final Map<UUID, OffenceResult> resultedOffences) {
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.resultedOffences = resultedOffences;
    }

    private DefendantCaseWithdrawnOrDismissed(final Builder builder) {
        caseId = builder.caseId;
        defendantId = builder.defendantId;
        resultedOffences = builder.resultedOffences;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final DefendantCaseWithdrawnOrDismissed copy) {
        final Builder builder = new Builder();
        builder.caseId = copy.getCaseId();
        builder.defendantId = copy.getDefendantId();
        builder.resultedOffences = copy.getResultedOffences();
        return builder;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public Map<UUID, OffenceResult> getResultedOffences() {
        return resultedOffences;
    }


    public static final class Builder {
        private UUID caseId;
        private UUID defendantId;
        private Map<UUID, OffenceResult> resultedOffences;

        private Builder() {
        }

        public Builder withCaseId(final UUID val) {
            caseId = val;
            return this;
        }

        public Builder withDefendantId(final UUID val) {
            defendantId = val;
            return this;
        }

        public Builder withResultedOffences(final Map<UUID, OffenceResult> val) {
            resultedOffences = val;
            return this;
        }

        public DefendantCaseWithdrawnOrDismissed build() {
            return new DefendantCaseWithdrawnOrDismissed(this);
        }
    }

    @Override
    public String toString() {
        return "DefendantCaseWithdrawnOrDismissed{" +
                "caseId=" + caseId +
                ", defendantId=" + defendantId +
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
        final DefendantCaseWithdrawnOrDismissed that = (DefendantCaseWithdrawnOrDismissed) o;
        return Objects.equals(caseId, that.caseId) &&
                Objects.equals(defendantId, that.defendantId) &&
                Objects.equals(resultedOffences, that.resultedOffences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseId, defendantId, resultedOffences);
    }
}
