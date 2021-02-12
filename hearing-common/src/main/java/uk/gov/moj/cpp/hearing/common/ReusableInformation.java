package uk.gov.moj.cpp.hearing.common;

import java.util.UUID;

public class ReusableInformation<T> {

    private T value;
    private String promptRef;
    private UUID masterDefendantId;
    private Integer cacheable;
    private String cacheDataPath;

    public ReusableInformation(final T value, final String promptRef, final UUID masterDefendantId, final Integer cacheable, final String cacheDataPath) {
        this.value = value;
        this.promptRef = promptRef;
        this.masterDefendantId = masterDefendantId;
        this.cacheable = cacheable;
        this.cacheDataPath = cacheDataPath;
    }

    public T getValue() {
        return value;
    }

    public String getPromptRef() {
        return promptRef;
    }

    public UUID getMasterDefendantId() {
        return masterDefendantId;
    }

    public Integer getCacheable() {
        return cacheable;
    }

    public String getCacheDataPath() {
        return cacheDataPath;
    }

    public static class Builder<T> {
        private T value;
        private String promptRef;
        private UUID masterDefendantId;
        private Integer cacheable;
        private String cacheDataPath;

        public Builder withValue(final T value) {
            this.value = value;
            return this;
        }

        public Builder withPromptRef(final String promptRef) {
            this.promptRef = promptRef;
            return this;
        }

        public Builder withMasterDefendantId(final UUID masterDefendantId) {
            this.masterDefendantId = masterDefendantId;
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

        public ReusableInformation build() {
            return new ReusableInformation(value, promptRef, masterDefendantId, cacheable, cacheDataPath);
        }
    }

}
