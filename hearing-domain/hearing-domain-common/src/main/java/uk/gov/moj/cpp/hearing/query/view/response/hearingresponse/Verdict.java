package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Verdict {

    private final UUID verdictTypeId;
    private final Value value;
    private final String verdictDate;
    private final Integer numberOfSplitJurors;
    private final Integer numberOfJurors;
    private final Boolean unanimous;

    @JsonCreator
    protected Verdict(@JsonProperty("verdictTypeId") final UUID verdictTypeId,
                    @JsonProperty("value") final Value value,
                    @JsonProperty("verdictDate") final String verdictDate,
                    @JsonProperty("numberOfSplitJurors") final Integer numberOfSplitJurors,
                    @JsonProperty("numberOfJurors") final Integer numberOfJurors,
                    @JsonProperty("unanimous") final Boolean unanimous) {
        this.verdictTypeId = verdictTypeId;
        this.value = value;
        this.verdictDate = verdictDate;
        this.numberOfSplitJurors = numberOfSplitJurors;
        this.numberOfJurors = numberOfJurors;
        this.unanimous = unanimous;
    }

    private Verdict(Builder builder) {
        this.verdictTypeId = builder.verdictTypeId;
        this.value = builder.value;
        this.verdictDate = builder.verdictDate;
        this.numberOfSplitJurors = builder.numberOfSplitJurors;
        this.numberOfJurors = builder.numberOfJurors;
        this.unanimous = builder.unanimous;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getVerdictTypeId() {
        return verdictTypeId;
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

    public static final class Builder {

        private UUID verdictTypeId;
        private Value value;
        private String verdictDate;
        private Integer numberOfSplitJurors;
        private Integer numberOfJurors;
        private Boolean unanimous;

        public Builder withVerdictTypeId(final UUID verdictId) {
            this.verdictTypeId = verdictId;
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