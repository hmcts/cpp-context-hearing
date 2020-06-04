package uk.gov.moj.cpp.hearing.command;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HearingVacatedTrialCleared {

    private final UUID hearingId;

    @JsonCreator
    public HearingVacatedTrialCleared(@JsonProperty("hearingId") final UUID hearingId) {
        this.hearingId = hearingId;
    }

    @JsonIgnore
    private HearingVacatedTrialCleared(final HearingVacatedTrialCleared.Builder builder) {
        this.hearingId = builder.hearingId;
    }

    public static HearingVacatedTrialCleared.Builder builder() {
        return new HearingVacatedTrialCleared.Builder();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public static final class Builder {

        private UUID hearingId;

        public HearingVacatedTrialCleared.Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public HearingVacatedTrialCleared build() {
            return new HearingVacatedTrialCleared(this);
        }
    }
}

