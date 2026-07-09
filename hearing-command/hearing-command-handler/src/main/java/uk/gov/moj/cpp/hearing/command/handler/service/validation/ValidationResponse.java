package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationResponse {

    private final boolean isValid;
    private final List<ValidationIssue> errors;
    private final List<ValidationIssue> warnings;

    @JsonCreator
    public ValidationResponse(
            @JsonProperty("isValid") final boolean isValid,
            @JsonProperty("errors") final List<ValidationIssue> errors,
            @JsonProperty("warnings") final List<ValidationIssue> warnings) {
        this.isValid = isValid;
        this.errors = errors != null ? errors : List.of();
        this.warnings = warnings != null ? warnings : List.of();
    }

    public static ValidationResponse passThrough() {
        return new ValidationResponse(true, List.of(), List.of());
    }

    public boolean isValid() {
        return isValid;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<ValidationIssue> getErrors() {
        return errors;
    }

    public List<ValidationIssue> getWarnings() {
        return warnings;
    }
}
