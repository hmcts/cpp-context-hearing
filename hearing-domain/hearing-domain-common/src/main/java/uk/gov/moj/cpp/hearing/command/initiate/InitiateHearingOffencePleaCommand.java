package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.UUID;

public final class InitiateHearingOffencePleaCommand {

    private UUID offenceId;
    private UUID caseId;
    private UUID defendantId;
    private UUID hearingId;
    private UUID originHearingId;
    private LocalDate pleaDate;
    private String value;

    @JsonCreator
    public InitiateHearingOffencePleaCommand(@JsonProperty("offenceId") UUID offenceId,
            @JsonProperty("caseId") UUID caseId, 
            @JsonProperty("defendantId") UUID defendantId,
            @JsonProperty("hearingId") UUID hearingId, 
            @JsonProperty("originHearingId") UUID originHearingId,
            @JsonProperty("pleaDate") LocalDate pleaDate, 
            @JsonProperty("value") String value) {
        this.offenceId = offenceId;
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.hearingId = hearingId;
        this.originHearingId = originHearingId;
        this.pleaDate = pleaDate;
        this.value = value;
    }
    
    @JsonIgnore
    private InitiateHearingOffencePleaCommand(final Builder builder) {
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
        
        public InitiateHearingOffencePleaCommand build() {
            return new InitiateHearingOffencePleaCommand(this);
        }
    }
}