package uk.gov.moj.cpp.hearing.command;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TrialType {
    private UUID hearingId;
    private UUID trialTypeId;
    private Boolean isEffectiveTrial;

    public TrialType() {
    }

    @JsonCreator
    public TrialType(@JsonProperty("hearingId") final UUID hearingId,
                     @JsonProperty("trialTypeId") final UUID trialTypeId,
                     @JsonProperty("isEffectiveTrial") final Boolean isEffectiveTrial) {
        this.hearingId = hearingId;
        this.trialTypeId = trialTypeId;
        this.isEffectiveTrial = isEffectiveTrial;
    }

    @JsonIgnore
    private TrialType(final Builder builder) {
        this.hearingId = builder.hearingId;
        this.trialTypeId = builder.trialTypeId;
        this.isEffectiveTrial = builder.isEffectiveTrial;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getTrialTypeId() {
        return trialTypeId;
    }

    public Boolean getIsEffectiveTrial() {
        return isEffectiveTrial;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID hearingId;
        private UUID trialTypeId;
        private Boolean isEffectiveTrial;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withTrialTypeId(final UUID trialTypeId) {
            this.trialTypeId = trialTypeId;
            return this;
        }

        public Builder withIsEffectiveTrial(final Boolean isEffectiveTrial) {
            this.isEffectiveTrial = isEffectiveTrial;
            return this;
        }

        public TrialType build() {
            return new TrialType(this);
        }
    }
}
