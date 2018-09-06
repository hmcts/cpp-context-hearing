package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Value {

    private final String category;
    private final String categoryType;
    private final LesserOffence lesserOffence;

    @JsonCreator
    private Value(@JsonProperty("category") String category,
                  @JsonProperty("categoryType") final String categoryType,
                  @JsonProperty("lesserOffence") final LesserOffence lesserOffence) {
        this.category = category;
        this.categoryType = categoryType;
        this.lesserOffence = lesserOffence;
    }

    private Value(final Builder builder) {
        this.category = builder.category;
        this.categoryType = builder.categoryType;
        this.lesserOffence = builder.lesserOffence;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCategory() {
        return category;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public LesserOffence getLesserOffence() {
        return lesserOffence;
    }

    public static final class Builder {

        private String category;
        private String categoryType;
        private LesserOffence lesserOffence;

        public Builder withCategory(final String category) {
            this.category = category;
            return this;
        }

        public Builder withCategoryType(final String categoryType) {
            this.categoryType = categoryType;
            return this;
        }

        public Builder withLesserOffence(final LesserOffence lesserOffence) {
            this.lesserOffence = lesserOffence;
            return this;
        }

        public Value build() {
            return new Value(this);
        }
    }
}