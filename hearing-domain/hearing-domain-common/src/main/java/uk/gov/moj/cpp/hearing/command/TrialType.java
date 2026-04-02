package uk.gov.moj.cpp.hearing.command;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TrialType {
    private UUID hearingId;
    private UUID trialTypeId;
    private Boolean isEffectiveTrial;
    private UUID vacatedTrialReasonId;
    private UUID crackedIneffectiveSubReasonId;

    public TrialType() {
    }

    @JsonCreator
    public TrialType(@JsonProperty("hearingId") final UUID hearingId,
                     @JsonProperty("trialTypeId") final UUID trialTypeId,
                     @JsonProperty("isEffectiveTrial") final Boolean isEffectiveTrial,
                     @JsonProperty("vacatedTrialReasonId") final UUID vacatedTrialReasonId,
                     @JsonProperty("crackedIneffectiveSubReasonId") final UUID crackedIneffectiveSubReasonId) {
        this.hearingId = hearingId;
        this.trialTypeId = trialTypeId;
        this.isEffectiveTrial = isEffectiveTrial;
        this.vacatedTrialReasonId = vacatedTrialReasonId;
        this.crackedIneffectiveSubReasonId = crackedIneffectiveSubReasonId;
    }

    @JsonIgnore
    private TrialType(final Builder builder) {
        this.hearingId = builder.hearingId;
        this.trialTypeId = builder.trialTypeId;
        this.isEffectiveTrial = builder.isEffectiveTrial;
        this.vacatedTrialReasonId = builder.vacatedTrialReasonId;
        this.crackedIneffectiveSubReasonId = builder.crackedIneffectiveSubReasonId;
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

    public UUID getVacatedTrialReasonId() {
        return vacatedTrialReasonId;
    }

    public UUID getCrackedIneffectiveSubReasonId(){
        return crackedIneffectiveSubReasonId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID hearingId;
        private UUID trialTypeId;
        private Boolean isEffectiveTrial;
        private UUID vacatedTrialReasonId;
        private UUID crackedIneffectiveSubReasonId;

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

        public Builder withVacatedTrialReasonId(final UUID vacatedTrialReasonId){
            this.vacatedTrialReasonId = vacatedTrialReasonId;
            return this;
        }

        public Builder withCrackedIneffectiveSubReasonId(final UUID crackedIneffectiveSubReasonId){
            this.crackedIneffectiveSubReasonId = crackedIneffectiveSubReasonId;
            return this;
        }

        public TrialType build() {
            return new TrialType(this);
        }
    }
}
