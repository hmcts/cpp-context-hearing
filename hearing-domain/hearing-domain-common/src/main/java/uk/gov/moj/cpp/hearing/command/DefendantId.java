package uk.gov.moj.cpp.hearing.command;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S1700"})
public class DefendantId {

    private UUID defendantId;

    @JsonCreator
    public DefendantId(@JsonProperty("defendantId") UUID defendantId) {
        this.defendantId = defendantId;
    }

    @JsonIgnore
    private DefendantId(Builder builder) {
        this.defendantId = builder.defendantId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public static class Builder {
        private UUID defendantId;

        public Builder withDefendantId(UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public DefendantId build() {
            return new DefendantId(this);
        }
    }
}
