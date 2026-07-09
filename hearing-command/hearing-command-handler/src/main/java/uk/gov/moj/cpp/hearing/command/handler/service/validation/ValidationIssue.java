package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationIssue {

    private final String ruleId;
    private final String severity;
    private final String message;
    private final List<AffectedOffence> affectedOffences;

    @JsonCreator
    public ValidationIssue(
            @JsonProperty("ruleId") final String ruleId,
            @JsonProperty("severity") final String severity,
            @JsonProperty("message") final String message,
            @JsonProperty("affectedOffences") final List<AffectedOffence> affectedOffences) {
        this.ruleId = ruleId;
        this.severity = severity;
        this.message = message;
        this.affectedOffences = affectedOffences != null ? affectedOffences : List.of();
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

    public List<AffectedOffence> getAffectedOffences() {
        return affectedOffences;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AffectedOffence {

        private final String id;
        private final String title;

        @JsonCreator
        public AffectedOffence(
                @JsonProperty("id") final String id,
                @JsonProperty("title") final String title) {
            this.id = id;
            this.title = title;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }
    }
}
