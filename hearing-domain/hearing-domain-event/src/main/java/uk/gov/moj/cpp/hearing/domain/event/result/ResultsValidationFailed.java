package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;

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
    private List<ValidationError> errors;
    private List<ValidationError> warnings;

    @JsonCreator
    private ResultsValidationFailed(
            @JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("hearingDay") final LocalDate hearingDay,
            @JsonProperty("userId") final String userId,
            @JsonProperty("errors") final List<ValidationError> errors,
            @JsonProperty("warnings") final List<ValidationError> warnings) {
        this.hearingId = hearingId;
        this.hearingDay = hearingDay;
        this.userId = userId;
        this.errors = errors;
        this.warnings = warnings;
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

    public List<ValidationError> getErrors() {
        return errors;
    }

    public List<ValidationError> getWarnings() {
        return warnings;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class ValidationError implements Serializable {

        private static final long serialVersionUID = 1L;

        private String ruleId;
        private String severity;
        private String message;
        private List<String> affectedOffences;

        @JsonCreator
        public ValidationError(
                @JsonProperty("ruleId") final String ruleId,
                @JsonProperty("severity") final String severity,
                @JsonProperty("message") final String message,
                @JsonProperty("affectedOffences") final List<String> affectedOffences) {
            this.ruleId = ruleId;
            this.severity = severity;
            this.message = message;
            this.affectedOffences = affectedOffences;
        }

        public ValidationError() {
        }

        public String getRuleId() {
            return ruleId;
        }

        public String getSeverity() {
            return severity;
        }

        public String getMessage() {
            return message;
        }

        public List<String> getAffectedOffences() {
            return affectedOffences;
        }
    }

    public static final class Builder {

        private UUID hearingId;
        private LocalDate hearingDay;
        private String userId;
        private List<ValidationError> errors;
        private List<ValidationError> warnings;

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

        public Builder withErrors(final List<ValidationError> errors) {
            this.errors = errors;
            return this;
        }

        public Builder withWarnings(final List<ValidationError> warnings) {
            this.warnings = warnings;
            return this;
        }

        public ResultsValidationFailed build() {
            return new ResultsValidationFailed(hearingId, hearingDay, userId, errors, warnings);
        }
    }
}
