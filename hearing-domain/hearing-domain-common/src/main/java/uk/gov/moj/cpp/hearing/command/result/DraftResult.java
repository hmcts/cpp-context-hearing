package uk.gov.moj.cpp.hearing.command.result;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
public final class DraftResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID targetId;
    private final UUID caseId;
    private final UUID defendantId;
    private final UUID offenceId;
    private final Integer offenceNum;
    private final Boolean showDefendantName;
    private final Boolean addMoreResults;
    private final List<Result> results;

    @JsonCreator
    public DraftResult(@JsonProperty("targetId") final UUID targetId,
                       @JsonProperty("caseId") final UUID caseId,
                       @JsonProperty("defendantId") final UUID defendantId,
                       @JsonProperty("offenceId") final UUID offenceId,
                       @JsonProperty("offenceNum") final Integer offenceNum,
                       @JsonProperty("showDefendantName") final Boolean showDefendantName,
                       @JsonProperty("addMoreResults") final Boolean addMoreResults,
                       @JsonProperty("results") final List<Result> results) {
        this.targetId = targetId;
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.offenceId = offenceId;
        this.offenceNum = offenceNum;
        this.showDefendantName = showDefendantName;
        this.addMoreResults = addMoreResults;
        this.results = unmodifiableList(ofNullable(results).orElseGet(ArrayList::new));
    }

    @JsonIgnore
    private DraftResult(final Builder builder) {
        this.targetId = builder.targetId;
        this.caseId = builder.caseId;
        this.defendantId = builder.defendantId;
        this.offenceId = builder.offenceId;
        this.offenceNum = builder.offenceNum;
        this.showDefendantName = builder.showDefendantName;
        this.addMoreResults = builder.addMoreResults;
        this.results = unmodifiableList(ofNullable(builder.results).orElseGet(ArrayList::new));
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getTargetId() {
        return targetId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public Integer getOffenceNum() {
        return offenceNum;
    }

    public Boolean getShowDefendantName() {
        return showDefendantName;
    }

    public Boolean getAddMoreResults() {
        return addMoreResults;
    }

    public List<Result> getResults() {
        return results;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DraftResult that = (DraftResult) o;
        return Objects.equals(this.targetId, that.targetId) && Objects.equals(this.caseId, that.caseId)
                && Objects.equals(this.defendantId, that.defendantId)
                && Objects.equals(this.offenceNum, that.offenceNum)
                && Objects.equals(this.showDefendantName, that.showDefendantName)
                && Objects.equals(this.addMoreResults, that.addMoreResults)
                && Objects.equals(this.results, that.results);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.targetId, this.caseId, this.defendantId, this.offenceId, this.offenceNum,
                this.showDefendantName, this.addMoreResults, this.results);
    }

    public static final class Builder {

        private UUID targetId;
        private UUID caseId;
        private UUID defendantId;
        private UUID offenceId;
        private Integer offenceNum;
        private Boolean showDefendantName;
        private Boolean addMoreResults;
        private List<Result> results;

        public Builder withTargetId(final UUID targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public Builder withOffenceNum(final Integer offenceNum) {
            this.offenceNum = offenceNum;
            return this;
        }

        public Builder withShowDefendantName(final Boolean showDefendantName) {
            this.showDefendantName = showDefendantName;
            return this;
        }

        public Builder withAddMoreResults(final Boolean addMoreResults) {
            this.addMoreResults = addMoreResults;
            return this;
        }

        public Builder withResults(final List<Result> results) {
            this.results = results;
            return this;
        }

        public DraftResult build() {
            return new DraftResult(this);
        }
    }
}