package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.inherited-plea")
public final class InheritedPlea implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID offenceId;
    private final UUID caseId;
    private final UUID defendantId;
    private final UUID hearingId;
    private final UUID originHearingId;
    private final LocalDate pleaDate;
    private final String value;

    @JsonCreator
    public InheritedPlea(@JsonProperty("offenceId") final UUID offenceId,
                         @JsonProperty("caseId") final UUID caseId,
                         @JsonProperty("defendantId") final UUID defendantId,
                         @JsonProperty("hearingId") final UUID hearingId,
                         @JsonProperty("originHearingId") final UUID originHearingId,
                         @JsonProperty("pleaDate") final LocalDate pleaDate,
                         @JsonProperty("value") final String value) {
        this.offenceId = offenceId;
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.hearingId = hearingId;
        this.originHearingId = originHearingId;
        this.pleaDate = pleaDate;
        this.value = value;
    }

    @JsonIgnore
    private InheritedPlea(final Builder builder) {
        this.offenceId = builder.offenceId;
        this.caseId = builder.caseId;
        this.defendantId = builder.defendantId;
        this.hearingId = builder.hearingId;
        this.originHearingId = builder.originHearingId;
        this.pleaDate = builder.pleaDate;
        this.value = builder.value;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getOriginHearingId() {
        return originHearingId;
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
    
        private UUID offenceId;
        private UUID caseId;
        private UUID defendantId;
        private UUID hearingId;
        private UUID originHearingId;
        private LocalDate pleaDate;
        private String value;
        
        public Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }
        
        public Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
            return this;
        }
        
        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }
        
        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }
        
        public Builder withOriginHearingId(final UUID originHearingId) {
            this.originHearingId = originHearingId;
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
        
        public InheritedPlea build() {
            return new InheritedPlea(this);
        }
    }
}
