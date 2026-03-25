package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
public class ResultLineDto {

    private final String id;
    private final String shortCode;
    private final String label;
    private final String defendantId;
    private final String offenceId;
    @JsonProperty("isConcurrent")
    private final Boolean isConcurrent;
    private final String consecutiveToOffence;

    private ResultLineDto(Builder builder) {
        this.id = builder.id;
        this.shortCode = builder.shortCode;
        this.label = builder.label;
        this.defendantId = builder.defendantId;
        this.offenceId = builder.offenceId;
        this.isConcurrent = builder.isConcurrent;
        this.consecutiveToOffence = builder.consecutiveToOffence;
    }

    // Getters
    public String getId() { return id; }
    public String getShortCode() { return shortCode; }
    public String getLabel() { return label; }
    public String getDefendantId() { return defendantId; }
    public String getOffenceId() { return offenceId; }
    public Boolean getIsConcurrent() { return isConcurrent; }
    public String getConsecutiveToOffence() { return consecutiveToOffence; }

    // Builder
    public static class Builder {
        private String id;
        private String shortCode;
        private String label;
        private String defendantId;
        private String offenceId;
        private Boolean isConcurrent;
        private String consecutiveToOffence;

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

        public ResultLineDto build() {
            return new ResultLineDto(this);
        }
    }
}
