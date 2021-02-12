package uk.gov.moj.cpp.hearing.command;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReusableInfoResults {

    private String shortCode;
    private UUID masterDefendantId;
    private String value;
    private UUID offenceId;

    public ReusableInfoResults(){

    }

    @JsonCreator
    public ReusableInfoResults(@JsonProperty("shortCode") final String shortCode,
                               @JsonProperty("masterDefendantId") final UUID masterDefendantId,
                               @JsonProperty("value") final String value,
                               @JsonProperty("offenceId") final UUID offenceId){
        this.shortCode = shortCode;
        this.masterDefendantId = masterDefendantId;
        this.value = value;
        this.offenceId = offenceId;
    }

    @JsonIgnore
    private ReusableInfoResults(Builder builder) {
        this.shortCode = builder.shortCode;
        this.masterDefendantId = builder.masterDefendantId;
        this.value = builder.value;
        this.offenceId = builder.offenceId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getShortCode() {
        return shortCode;
    }

    public UUID getMasterDefendantId() {
        return masterDefendantId;
    }

    public String getValue() {
        return value;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public static class Builder {
        private String shortCode;
        private UUID masterDefendantId;
        private String value;
        private UUID offenceId;

        public Builder withShortCode(final String shortCode) {
            this.shortCode = shortCode;
            return this;
        }

        public Builder withMasterDefendantId(final UUID masterDefendantId) {
            this.masterDefendantId = masterDefendantId;
            return this;
        }

        public Builder withValue(final String value) {
            this.value = value;
            return this;
        }

        public Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public ReusableInfoResults build(){
            return new ReusableInfoResults(shortCode, masterDefendantId, value, offenceId);
        }
    }
}
