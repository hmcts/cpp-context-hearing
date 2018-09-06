package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.json.schemas.core.DelegatedPowers;

public class Plea {

    private String pleaId;
    private String pleaDate;
    private String value;
    private DelegatedPowers delegatedPowers;

    @JsonCreator
    public Plea(@JsonProperty("pleaId") final String pleaId,
                 @JsonProperty("pleaDate") final String pleaDate,
                 @JsonProperty("value") final String value,
                 @JsonProperty("delegatedPowers") final DelegatedPowers delegatedPowers) {
        this.pleaId = pleaId;
        this.pleaDate = pleaDate;
        this.value = value;
        this.delegatedPowers = delegatedPowers;
    }

    private Plea(final Builder builder) {
        this.pleaId = builder.pleaId;
        this.pleaDate = builder.pleaDate;
        this.value = builder.value;
        this.delegatedPowers = builder.delegatedPowers;
    }

    public static Builder builder() {
        return new Builder();
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

    public DelegatedPowers getDelegatedPowers() {
        return delegatedPowers;
    }

    public static final class Builder {

        private String pleaId;
        private String pleaDate;
        private String value;
        private DelegatedPowers delegatedPowers;

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

        public Builder withDelegatedPowers(DelegatedPowers delegatedPowers) {
            this.delegatedPowers = delegatedPowers;
            return this;
        }

        public Plea build() {
            return new Plea(this);
        }
    }
}