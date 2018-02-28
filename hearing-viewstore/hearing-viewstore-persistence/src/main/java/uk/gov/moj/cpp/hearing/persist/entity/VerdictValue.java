package uk.gov.moj.cpp.hearing.persist.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class VerdictValue implements Serializable {

    @Column(name = "value_id")
    private UUID id;

    @Column(name = "value_category")
    private String category;

    @Column(name = "value_description")
    private String description;

    @Column(name = "value_code")
    private String code;
    
    public VerdictValue() {
        //For JPA
    }
    
    private VerdictValue(final Builder builder) {
        assert null != builder;
        this.id = builder.id;
        this.category = builder.category;
        this.code = builder.code;
        this.description = builder.description;
    }

    public UUID getId() {
        return id;
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

    public static final class Builder {
        
        private UUID id;
        private String category;
        private String description;
        private String code;
        
        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }
        
        public Builder withCategory(final String category) {
            this.category = category;
            return this;
        }
        
        public Builder withDescription(final String description) {
            this.description = description;
            return this;
        }
        
        public Builder withCode(final String code) {
            this.code = code;
            return this;
        }
        
        public VerdictValue build() {
            return new VerdictValue(this);
        }
    }

}
