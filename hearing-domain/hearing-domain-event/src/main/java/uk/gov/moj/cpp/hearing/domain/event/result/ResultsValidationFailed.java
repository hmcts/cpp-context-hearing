package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.ValidationErrors;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.ValidationIssue;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "PMD.BeanMembersShouldSerialize"})
@Event("hearing.events.results-validation-failed")
public class ResultsValidationFailed implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private LocalDate hearingDay;
    private String userId;
    private String validationId;
    private String timestamp;
    private String mode;
    private List<String> rulesEvaluated;
    private boolean isValid;
    private ValidationErrors errors;
    private List<ValidationIssue> warnings;
    private Integer processingTimeMs;

    @JsonCreator
    private ResultsValidationFailed(
            @JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("hearingDay") final LocalDate hearingDay,
            @JsonProperty("userId") final String userId,
            @JsonProperty("validationId") final String validationId,
            @JsonProperty("timestamp") final String timestamp,
            @JsonProperty("mode") final String mode,
            @JsonProperty("rulesEvaluated") final List<String> rulesEvaluated,
            @JsonProperty("isValid") final boolean isValid,
            @JsonProperty("errors") final ValidationErrors errors,
            @JsonProperty("warnings") final List<ValidationIssue> warnings,
            @JsonProperty("processingTimeMs") final Integer processingTimeMs) {
        this.hearingId = hearingId;
        this.hearingDay = hearingDay;
        this.userId = userId;
        this.validationId = validationId;
        this.timestamp = timestamp;
        this.mode = mode;
        this.rulesEvaluated = rulesEvaluated;
        this.isValid = isValid;
        this.errors = errors;
        this.warnings = warnings;
        this.processingTimeMs = processingTimeMs;
    }

    public ResultsValidationFailed() {
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public String getUserId() {
        return userId;
    }

    public String getValidationId() {
        return validationId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMode() {
        return mode;
    }

    public List<String> getRulesEvaluated() {
        return rulesEvaluated;
    }

    @JsonProperty("isValid")
    public boolean isValid() {
        return isValid;
    }

    public ValidationErrors getErrors() {
        return errors;
    }

    public List<ValidationIssue> getWarnings() {
        return warnings;
    }

    public Integer getProcessingTimeMs() {
        return processingTimeMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID hearingId;
        private LocalDate hearingDay;
        private String userId;
        private String validationId;
        private String timestamp;
        private String mode;
        private List<String> rulesEvaluated;
        private boolean isValid;
        private ValidationErrors errors;
        private List<ValidationIssue> warnings;
        private Integer processingTimeMs;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withHearingDay(final LocalDate hearingDay) {
            this.hearingDay = hearingDay;
            return this;
        }

        public Builder withUserId(final String userId) {
            this.userId = userId;
            return this;
        }

        public Builder withValidationId(final String validationId) {
            this.validationId = validationId;
            return this;
        }

        public Builder withTimestamp(final String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withMode(final String mode) {
            this.mode = mode;
            return this;
        }

        public Builder withRulesEvaluated(final List<String> rulesEvaluated) {
            this.rulesEvaluated = rulesEvaluated;
            return this;
        }

        public Builder withIsValid(final boolean isValid) {
            this.isValid = isValid;
            return this;
        }

        public Builder withErrors(final ValidationErrors errors) {
            this.errors = errors;
            return this;
        }

        public Builder withWarnings(final List<ValidationIssue> warnings) {
            this.warnings = warnings;
            return this;
        }

        public Builder withProcessingTimeMs(final Integer processingTimeMs) {
            this.processingTimeMs = processingTimeMs;
            return this;
        }

        public ResultsValidationFailed build() {
            return new ResultsValidationFailed(hearingId, hearingDay, userId, validationId, timestamp, mode,
                    rulesEvaluated, isValid, errors, warnings, processingTimeMs);
        }
    }
}
