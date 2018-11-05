package uk.gov.moj.cpp.hearing.command.verdict;


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("squid:S1067")
public class VerdictValue implements Serializable {

    private static final long serialVersionUID = 1L;

    final private UUID id;
    final private UUID verdictTypeId;
    final private String category;
    final private String categoryType;
    final private String lesserOffence;
    final private String code;
    final private String description;

    @JsonCreator
    public VerdictValue(@JsonProperty("id") final UUID id,
                        @JsonProperty("verdictTypeId") final UUID verdictTypeId,
                        @JsonProperty("category") final String category,
                        @JsonProperty("categoryType") final String categoryType,
                        @JsonProperty("lesserOffence") final String lesserOffence,
                        @JsonProperty("code") final String code,
                        @JsonProperty("description") final String description) {
        this.id = id;
        this.verdictTypeId = verdictTypeId;
        this.category = category;
        this.categoryType = categoryType;
        this.lesserOffence = lesserOffence;
        this.code = code;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public UUID getVerdictTypeId() {
        return verdictTypeId;
    }

    public String getCategory() {
        return category;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public String getLesserOffence() {
        return lesserOffence;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VerdictValue)) {
            return false;
        }
        VerdictValue verdict = (VerdictValue) o;
        return Objects.equals(getId(), verdict.getId()) &&
                Objects.equals(getVerdictTypeId(), verdict.getVerdictTypeId()) &&
                Objects.equals(getCategory(), verdict.getCategory()) &&
                Objects.equals(getCategoryType(), verdict.getCategoryType()) &&
                Objects.equals(getLesserOffence(), verdict.getLesserOffence()) &&
                Objects.equals(getCode(), verdict.getCode()) &&
                Objects.equals(getDescription(), verdict.getDescription());

    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getVerdictTypeId(), getCategory(), getCategoryType(), getLesserOffence(),
                getCode(), getDescription());
    }

    public static Builder from(VerdictValue value) {
        return new Builder()
                .withId(value.getId())
                .withVerdictTypeId(value.getVerdictTypeId())
                .withCategory(value.getCategory())
                .withCategoryType(value.getCategoryType())
                .withLesserOffence(value.getLesserOffence())
                .withCode(value.getCode())
                .withDescription(value.getDescription());
    }

    public static class Builder {
        private UUID id;
        private UUID verdictTypeId;
        private String category;
        private String categoryType;
        private String lesserOffence;
        private String code;
        private String description;

        public UUID getId() {
            return id;
        }

        public UUID getVerdictTypeId() {
            return verdictTypeId;
        }

        public String getCategory() {
            return category;
        }

        public String getCategoryType() {
            return categoryType;
        }

        public String getLesserOffence() {
            return lesserOffence;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withVerdictTypeId(UUID verdictTypeId) {
            this.verdictTypeId = verdictTypeId;
            return this;
        }
        
        public Builder withCategory(String category) {
            this.category = category;
            return this;
        }
        
        public Builder withCategoryType(String categoryType) {
            this.categoryType = categoryType;
            return this;
        }

        public Builder withLesserOffence(String lesserOffence) {
            this.lesserOffence = lesserOffence;
            return this;
        }
        
        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public VerdictValue build() {
            return new VerdictValue(id, verdictTypeId, category, categoryType, lesserOffence, code, description);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
