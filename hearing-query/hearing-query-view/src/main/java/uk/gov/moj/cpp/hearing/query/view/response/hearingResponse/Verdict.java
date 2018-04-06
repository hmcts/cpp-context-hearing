package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Verdict {

    private final String verdictId;
    private final String hearingId;
    private final Value value;
    private final String verdictDate;
    private final Integer numberOfSplitJurors;
    private final Integer numberOfJurors;
    private final Boolean unanimous;
    
    @JsonCreator
    public Verdict(@JsonProperty("verdictId") final String verdictId, 
            @JsonProperty("hearingId") final String hearingId, 
            @JsonProperty("value") final Value value, 
            @JsonProperty("verdictDate") final String verdictDate, 
            @JsonProperty("numberOfSplitJurors") final Integer numberOfSplitJurors,
            @JsonProperty("numberOfJurors") final Integer numberOfJurors, 
            @JsonProperty("unanimous") final Boolean unanimous) {
        this.verdictId = verdictId;
        this.hearingId = hearingId;
        this.value = value;
        this.verdictDate = verdictDate;
        this.numberOfSplitJurors = numberOfSplitJurors;
        this.numberOfJurors = numberOfJurors;
        this.unanimous = unanimous;
    }

    @JsonIgnore
    private Verdict(Builder builder) {
        this.verdictId = builder.verdictId;
        this.hearingId = builder.hearingId;
        this.value = builder.value;
        this.verdictDate = builder.verdictDate;
        this.numberOfSplitJurors = builder.numberOfSplitJurors;
        this.numberOfJurors = builder.numberOfJurors;
        this.unanimous = builder.unanimous;
    }
    
    public String getVerdictId() {
        return verdictId;
    }

    public String getHearingId() {
        return hearingId;
    }

    public Value getValue() {
        return value;
    }

    public String getVerdictDate() {
        return verdictDate;
    }

    public Integer getNumberOfSplitJurors() {
        return numberOfSplitJurors;
    }

    public Integer getNumberOfJurors() {
        return numberOfJurors;
    }

    public Boolean getUnanimous() {
        return unanimous;
    }

    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {

        private String verdictId;
        private String hearingId;
        private Value value;
        private String verdictDate;
        private Integer numberOfSplitJurors;
        private Integer numberOfJurors;
        private Boolean unanimous;
        
        public Builder withVerdictId(final String verdictId) {
            this.verdictId = verdictId;
            return this;
        }
        public Builder withHearingId(final String hearingId) {
            this.hearingId = hearingId;
            return this;
        }
        public Builder withValue(final Value value) {
            this.value = value;
            return this;
        }
        public Builder withVerdictDate(final String verdictDate) {
            this.verdictDate = verdictDate;
            return this;
        }
        
        public Builder withNumberOfSplitJurors(final Integer numberOfSplitJurors) {
            this.numberOfSplitJurors = numberOfSplitJurors;
            return this;
        }
        public Builder withNumberOfJurors(final Integer numberOfJurors) {
            this.numberOfJurors = numberOfJurors;
            return this;
        }
        public Builder withUnanimous(final Boolean unanimous) {
            this.unanimous = unanimous;
            return this;
        }
        
        public Verdict build() {
            return new Verdict(this);
        }
    }
}