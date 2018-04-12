package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

public final class Value {

    private final String category;
    private final String code;
    private final String description;

    private Value(final Builder builder) {
        this.category = builder.category;
        this.code = builder.code;
        this.description = builder.description;
    }

    public String getCategory() {
        return category;
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
        private String code;
        private String description;
        
        public Builder withCategory(final String category) {
            this.category = category;
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