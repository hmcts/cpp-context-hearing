package uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class IndicatedPlea {
    private final AllocationDecision allocationDecision;

    private final UUID id;

    private final IndicatedPleaValue value;

    @JsonCreator
    public IndicatedPlea(
            @JsonProperty("allocationDecision") final AllocationDecision allocationDecision,
            @JsonProperty("id") final UUID id,
            @JsonProperty("value") final IndicatedPleaValue value) {
        this.allocationDecision = allocationDecision;
        this.id = id;
        this.value = value;
    }

    public AllocationDecision getAllocationDecision() {
        return allocationDecision;
    }

    public UUID getId() {
        return id;
    }

    public IndicatedPleaValue getValue() {
        return value;
    }

    public static Builder indicatedPlea() {
        return new IndicatedPlea.Builder();
    }

    public static class Builder {
        private AllocationDecision allocationDecision;

        private UUID id;

        private IndicatedPleaValue indicatedPleaValue;

        public Builder withAllocationDecision(final AllocationDecision allocationDecision) {
            this.allocationDecision = allocationDecision;
            return this;
        }

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withIndicatedPleaValue(final IndicatedPleaValue indicatedPleaValue) {
            this.indicatedPleaValue = indicatedPleaValue;
            return this;
        }

        public IndicatedPlea build() {
            return new IndicatedPlea(allocationDecision, id, indicatedPleaValue);
        }
    }
}
