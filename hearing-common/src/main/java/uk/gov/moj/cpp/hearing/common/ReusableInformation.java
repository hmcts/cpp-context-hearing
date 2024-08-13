package uk.gov.moj.cpp.hearing.common;

import java.util.UUID;

public class ReusableInformation<T> {
    public enum IdType{
        DEFENDANT,
        CASE,
        APPLICATION
    }
    private T value;
    private String promptRef;
    private UUID masterDefendantId;
    private UUID caseId;
    private UUID applicationId;
    private Integer cacheable;
    private String cacheDataPath;

    public ReusableInformation(final T value, final String promptRef, final IdType idType, final UUID id, final Integer cacheable, final String cacheDataPath) {
        this.value = value;
        this.promptRef = promptRef;
        this.cacheable = cacheable;
        this.cacheDataPath = cacheDataPath;
        switch (idType) {
            case DEFENDANT:
                this.masterDefendantId = id;
                break;
            case CASE:
                this.caseId = id;
                break;
            case APPLICATION:
                this.applicationId = id;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + idType);
        }
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

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getApplicationId() {
        return applicationId;
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
        private UUID id;
        private Integer cacheable;
        private String cacheDataPath;
        private IdType idType;

        public Builder withValue(final T value) {
            this.value = value;
            return this;
        }

        public Builder withPromptRef(final String promptRef) {
            this.promptRef = promptRef;
            return this;
        }

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withIdType(final IdType idType) {
            this.idType = idType;
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
            return new ReusableInformation(value, promptRef, idType, id, cacheable, cacheDataPath);
        }
    }

}
