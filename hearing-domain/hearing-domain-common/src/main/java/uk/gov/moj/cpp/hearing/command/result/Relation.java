package uk.gov.moj.cpp.hearing.command.result;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
public final class Relation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID resultLineId;
    private final String ruleType;
    private final List<UUID> childResultLineIds;

    @JsonCreator
    protected Relation(@JsonProperty("resultLineId") final UUID resultLineId,
                       @JsonProperty("ruleType") final String ruleType,
                       @JsonProperty("childResultLineIds") final List<UUID> childResultLineIds) {

        this.resultLineId = resultLineId;
        this.ruleType = ruleType;
        this.childResultLineIds = childResultLineIds;
    }

    @JsonIgnore
    private Relation(final Builder builder) {
        this.resultLineId = builder.resultLineId;
        this.ruleType = builder.ruleType;
        this.childResultLineIds = builder.childResultLineIds;
    }

    public static Relation.Builder builder() {
        return new Relation.Builder();
    }

    public UUID getResultLineId() {
        return resultLineId;
    }

    public String getRuleType() {
        return ruleType;
    }

    public List<UUID> getChildResultLineIds() {
        return childResultLineIds;
    }

    public static final class Builder {

        private UUID resultLineId;
        private String ruleType;
        private List<UUID> childResultLineIds;

        public Builder withResultLineId(final UUID resultLineId) {
            this.resultLineId = resultLineId;
            return this;
        }

        public Builder withRuleType(final String ruleType) {
            this.ruleType = ruleType;
            return this;
        }

        public Builder withState(final List<UUID> childResultLineIds) {
            this.childResultLineIds = childResultLineIds;
            return this;
        }

        public Relation build() {
            return new Relation(this);
        }
    }
}