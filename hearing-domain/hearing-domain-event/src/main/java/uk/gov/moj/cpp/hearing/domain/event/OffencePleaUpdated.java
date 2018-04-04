package uk.gov.moj.cpp.hearing.domain.event;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.justice.domain.annotation.Event;

@Event("hearing.offence-plea-updated")
public final class OffencePleaUpdated {

    private final UUID originHearingId;
    private final UUID offenceId;
    private final LocalDate pleaDate;
    private final String value;

    @JsonCreator
    public OffencePleaUpdated(@JsonProperty("originHearingId") final UUID originHearingId, 
            @JsonProperty("offenceId") final UUID offenceId, 
            @JsonProperty("pleaDate") final LocalDate pleaDate, 
            @JsonProperty("value") final String value) {
        this.originHearingId = originHearingId;
        this.offenceId = offenceId;
        this.pleaDate = pleaDate;
        this.value = value;
    }

    @JsonIgnore // avoid serialisation by this constructor
    private OffencePleaUpdated(final Builder builder) {
        this.originHearingId = builder.originHearingId;
        this.offenceId = builder.offenceId;
        this.pleaDate = builder.pleaDate;
        this.value = builder.value;
    }

    public UUID getOriginHearingId() {
        return originHearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public LocalDate getPleaDate() {
        return pleaDate;
    }

    public String getValue() {
        return value;
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID originHearingId;
        private UUID offenceId;
        private LocalDate pleaDate;
        private String value;
        
        public Builder withOriginHearingId(final UUID originHearingId) {
            this.originHearingId = originHearingId;
            return this;
        }
        public Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }
        public Builder withPleaDate(final LocalDate pleaDate) {
            this.pleaDate = pleaDate;
            return this;
        }
        public Builder withValue(final String value) {
            this.value = value;
            return this;
        }
        
        public OffencePleaUpdated build() {
            return new OffencePleaUpdated(this);
        }
        
        public Stream<Object> buildStream() {
            return Stream.of(build());
        }
    }
}