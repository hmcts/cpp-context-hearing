package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

public final class Value {

    private final String category;
    private final String categoryType;
    private final String lesserOffence;
    private final String code;
    private final String description;
    private final String verdictTypeId;

    private Value(final Builder builder) {
        this.verdictTypeId = builder.verdictTypeId;
        this.category = builder.category;
        this.categoryType = builder.categoryType;
        this.lesserOffence = builder.lesserOffence;
        this.code = builder.code;
        this.description = builder.description;
    }

    public String getVerdictTypeId() {
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

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String category;
        private String categoryType;
        private String lesserOffence;
        private String code;
        private String description;
        private String verdictTypeId;
        
        public Builder withVerdictTypeId(final String verdictTypeId) {
            this.verdictTypeId = verdictTypeId;
            return this;
        }
        
        public Builder withCategory(final String category) {
            this.category = category;
            return this;
        }
        
        public Builder withCategoryType(final String categoryType) {
            this.categoryType = categoryType;
            return this;
        }
        
        public Builder withLesserOffence(final String lesserOffence) {
            this.lesserOffence = lesserOffence;
            return this;
        }
        
        public Builder withCode(final String code) {
            this.code = code;
            return this;
        }
        
        public Builder withDescription(final String description) {
            this.description = description;
            return this;
        }
        
        public Value build() {
            return new Value(this);
        }
    }
}