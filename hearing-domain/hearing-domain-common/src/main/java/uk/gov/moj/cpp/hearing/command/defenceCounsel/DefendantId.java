package uk.gov.moj.cpp.hearing.command.defenceCounsel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class DefendantId {

    private UUID defendantId;

    @JsonCreator
    public DefendantId(@JsonProperty("defendantId") UUID defendantId) {
        this.defendantId = defendantId;
    }

    public DefendantId(Builder builder){
        this.defendantId = builder.defendantId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public static class Builder {
        private UUID defendantId;

        public Builder withDefendantId(UUID defendantId){
            this.defendantId = defendantId;
            return this;
        }

        public DefendantId build(){
            return new DefendantId(this);
        }
    }

    public static Builder builder(){
        return new Builder();
    }
}
