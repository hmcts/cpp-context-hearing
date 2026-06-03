package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ResultLineDto {

    private final String id;
    private final String shortCode;
    private final String label;
    private final String defendantId;
    private final String offenceId;
    @JsonProperty("isConcurrent")
    private final Boolean isConcurrent;
    private final String consecutiveToOffence;
    private final String category;
    private final List<PromptDto> prompts;

    private ResultLineDto(Builder builder) {
        this.id = builder.id;
        this.shortCode = builder.shortCode;
        this.label = builder.label;
        this.defendantId = builder.defendantId;
        this.offenceId = builder.offenceId;
        this.isConcurrent = builder.isConcurrent;
        this.consecutiveToOffence = builder.consecutiveToOffence;
        this.category = builder.category;
        this.prompts = builder.prompts;
    }

    // Getters
    public String getId() { return id; }
    public String getShortCode() { return shortCode; }
    public String getLabel() { return label; }
    public String getDefendantId() { return defendantId; }
    public String getOffenceId() { return offenceId; }
    public Boolean getIsConcurrent() { return isConcurrent; }
    public String getConsecutiveToOffence() { return consecutiveToOffence; }
    public String getCategory() { return category; }
    public List<PromptDto> getPrompts() { return prompts; }

    // Builder
    public static class Builder {
        private String id;
        private String shortCode;
        private String label;
        private String defendantId;
        private String offenceId;
        private Boolean isConcurrent;
        private String consecutiveToOffence;
        private String category;
        private List<PromptDto> prompts;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder shortCode(String shortCode) {
            this.shortCode = shortCode;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder defendantId(String defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder offenceId(String offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public Builder isConcurrent(Boolean isConcurrent) {
            this.isConcurrent = isConcurrent;
            return this;
        }

        public Builder consecutiveToOffence(String consecutiveToOffence) {
            this.consecutiveToOffence = consecutiveToOffence;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder prompts(List<PromptDto> prompts) {
            this.prompts = prompts;
            return this;
        }

        public ResultLineDto build() {
            return new ResultLineDto(this);
        }
    }
}
