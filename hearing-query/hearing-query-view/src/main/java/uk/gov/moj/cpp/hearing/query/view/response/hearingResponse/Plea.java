package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

public final class Plea {

    private final String pleaId;
    private final String pleaDate;
    private final String value;

    private Plea(final Builder builder) {
        this.pleaId = builder.pleaId;
        this.pleaDate = builder.pleaDate;
        this.value = builder.value;
    }

    public String getPleaId() {
        return pleaId;
    }

    public String getPleaDate() {
        return pleaDate;
    }

    public String getValue() {
        return value;
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String pleaId;
        private String pleaDate;
        private String value;
        
        public Builder withPleaId(String pleaId) {
            this.pleaId = pleaId;
            return this;
        }
        
        public Builder withPleaDate(String pleaDate) {
            this.pleaDate = pleaDate;
            return this;
        }
        
        public Builder withValue(String value) {
            this.value = value;
            return this;
        }
        
        public Plea build() {
            return new Plea(this);
        }
    }
}