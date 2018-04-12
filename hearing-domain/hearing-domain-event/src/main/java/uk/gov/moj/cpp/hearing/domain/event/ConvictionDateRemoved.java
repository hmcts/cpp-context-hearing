package uk.gov.moj.cpp.hearing.domain.event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.justice.domain.annotation.Event;

@Event("hearing.conviction-date-removed")
public final class ConvictionDateRemoved {

    private final UUID hearingId;
    private final UUID offenceId;

    @JsonCreator
    public ConvictionDateRemoved(@JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("offenceId") final UUID offenceId) {
        this.hearingId = hearingId;
        this.offenceId = offenceId;
    }

    @JsonIgnore
    private ConvictionDateRemoved(final Builder builder) {
        this.hearingId = builder.hearingId;
        this.offenceId = builder.offenceId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {

        private UUID hearingId;
        private UUID offenceId;
        
        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }
        
        public Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }
        
        public ConvictionDateRemoved build() {
            return new ConvictionDateRemoved(this);
        }
    }
}