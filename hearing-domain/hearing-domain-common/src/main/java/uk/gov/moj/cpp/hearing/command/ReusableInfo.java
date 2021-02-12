package uk.gov.moj.cpp.hearing.command;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReusableInfo {

    private String promptRef;
    private UUID masterDefendantId;
    private String type;
    private Object value;
    private Integer cacheable;
    private String cacheDataPath;
    private UUID offenceId;

    public ReusableInfo(){

    }

    @JsonCreator
    public ReusableInfo(@JsonProperty("promptRef") final String promptRef,
                        @JsonProperty("masterDefendantId") final UUID masterDefendantId,
                        @JsonProperty("type") final String type,
                        @JsonProperty("value") final Object value,
                        @JsonProperty("cacheable") final Integer cacheable,
                        @JsonProperty("cacheDataPath") final String cacheDataPath,
                        @JsonProperty("offenceId") final UUID offenceId){
        this.promptRef = promptRef;
        this.masterDefendantId = masterDefendantId;
        this.type = type;
        this.value = value;
        this.cacheable = cacheable;
        this.cacheDataPath = cacheDataPath;
        this.offenceId = offenceId;
    }

    @JsonIgnore
    private ReusableInfo(Builder builder) {
        this.promptRef = builder.promptRef;
        this.masterDefendantId = builder.masterDefendantId;
        this.type = builder.type;
        this.value = builder.value;
        this.cacheable = builder.cacheable;
        this.cacheDataPath = builder.cacheDataPath;
        this.offenceId = builder.offenceId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getPromptRef() {
        return promptRef;
    }

    public UUID getMasterDefendantId() {
        return masterDefendantId;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public Integer getCacheable() {
        return cacheable;
    }

    public String getCacheDataPath() {
        return cacheDataPath;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public static class Builder {
        private String promptRef;
        private UUID masterDefendantId;
        private String type;
        private Object value;
        private Integer cacheable;
        private String cacheDataPath;
        private UUID offenceId;

        public Builder withPromptRef(final String promptRef) {
            this.promptRef = promptRef;
            return this;
        }

        public Builder withMasterDefendantId(final UUID masterDefendantId) {
            this.masterDefendantId = masterDefendantId;
            return this;
        }

        public Builder withType(final String type) {
            this.type = type;
            return this;
        }

        public Builder withValue(final Object value) {
            this.value = value;
            return this;
        }

        public Builder withCacheable(final Integer cacheable) {
            this.cacheable = cacheable;
            return this;
        }

        public Builder withCacheDataPath(final String cacheDataPath) {
            this.cacheDataPath = cacheDataPath;
            return this;
        }

        public Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public ReusableInfo build(){
            return new ReusableInfo(promptRef, masterDefendantId,type,value, cacheable, cacheDataPath, offenceId);
        }
    }
}
