package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class LesserOffence {

    private final UUID offenceDefinitionId;
    private final String offenceCode;
    private final String title;
    private final String legislation;
 
    @JsonCreator
    protected LesserOffence(@JsonProperty("offenceDefinitionId") final UUID offenceDefinitionId, 
            @JsonProperty("offenceCode") final String offenceCode, 
            @JsonProperty("title") final String title, 
            @JsonProperty("legislation") final String legislation) {
        super();
        this.offenceDefinitionId = offenceDefinitionId;
        this.offenceCode = offenceCode;
        this.title = title;
        this.legislation = legislation;
    }

    public UUID getOffenceDefinitionId() {
        return offenceDefinitionId;
    }

    public String getOffenceCode() {
        return offenceCode;
    }

    public String getTitle() {
        return title;
    }

    public String getLegislation() {
        return legislation;
    }

    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {

        private UUID offenceDefinitionId;
        private String offenceCode;
        private String title;
        private String legislation;

        public Builder withOffenceDefinitionId(final UUID offenceDefinitionId) {
            this.offenceDefinitionId = offenceDefinitionId;
            return this;
        }
        public Builder withOffenceCode(final String offenceCode) {
            this.offenceCode = offenceCode;
            return this;
        }
        public Builder withTitle(final String title) {
            this.title = title;
            return this;
        }
        public Builder withLegislation(final String legislation) {
            this.legislation = legislation;
            return this;
        }

        public LesserOffence build() {
            return new LesserOffence(this.offenceDefinitionId, this.offenceCode, this.title, this.legislation);
        }
    }
}