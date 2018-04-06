package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Plea {

    private final String pleaId;
    private final String pleaDate;
    private final String value;
    
    @JsonCreator
    public Plea(@JsonProperty("pleaId") final String pleaId, 
            @JsonProperty("pleaDate") final String pleaDate, 
            @JsonProperty("value") final String value) {
        this.pleaId = pleaId;
        this.pleaDate = pleaDate;
        this.value = value;
    }

    @JsonIgnore
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