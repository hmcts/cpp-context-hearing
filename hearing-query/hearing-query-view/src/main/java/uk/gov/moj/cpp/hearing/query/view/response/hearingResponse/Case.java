package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Case {
    
    private final String caseId;
    private final String caseUrn;
    private final List <Defendant> defendants;
    
    @JsonCreator
    public Case(@JsonProperty("caseId") final String caseId, 
            @JsonProperty("caseUrn") final String caseUrn, 
            @JsonProperty("defendants") final List<Defendant> defendants) {
        this.caseId = caseId;
        this.caseUrn = caseUrn;
        this.defendants = defendants;
    }

    @JsonIgnore
    private Case(final Builder builder) {
        this.caseId = builder.caseId;
        this.caseUrn = builder.caseUrn;
        this.defendants = builder.defendants;
    }
    
    public String getCaseId() {
        return caseId;
    }

    public String getCaseUrn() {
        return caseUrn;
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        
        private String caseId;
        private String caseUrn;
        private List<Defendant> defendants;
        
        public Builder withCaseId(final String caseId) {
            this.caseId = caseId;
            return this;
        }
        
        public Builder withCaseUrn(final String caseUrn) {
            this.caseUrn = caseUrn;
            return this;
        }
        
        public Builder withDefendants(final List<Defendant> defendants) {
            this.defendants = defendants;
            return this;
        }
        
        public Case build() {
            return new Case(this);
        }
    }
}