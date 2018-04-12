package uk.gov.moj.cpp.hearing.domain.event;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.justice.domain.annotation.Event;

@Event("hearing.conviction-date-added")
public final class ConvictionDateAdded {

    private final UUID hearingId;
    private final UUID offenceId;
    private final LocalDate convictionDate;

    @JsonCreator
    public ConvictionDateAdded(@JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("offenceId") final UUID offenceId,
            @JsonProperty("convictionDate") final LocalDate convictionDate) {
        this.hearingId = hearingId;
        this.offenceId = offenceId;
        this.convictionDate = convictionDate;
    }
    
    @JsonIgnore
    private ConvictionDateAdded(final Builder builder) {
        this.hearingId = builder.hearingId;
        this.offenceId = builder.offenceId;
        this.convictionDate = builder.convictionDate;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {

        private UUID hearingId;
        private UUID offenceId;
        private LocalDate convictionDate;
        

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }
        
        public Builder withConvictionDate(final LocalDate convictionDate) {
            this.convictionDate = convictionDate;
            return this;
        }
        
        public ConvictionDateAdded build() {
            return new ConvictionDateAdded(this);
        }
    }
}