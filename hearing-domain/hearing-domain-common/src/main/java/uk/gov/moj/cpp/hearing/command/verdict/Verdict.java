package uk.gov.moj.cpp.hearing.command.verdict;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("squid:S1067")
public class Verdict implements Serializable {
    private static final long serialVersionUID = 1L;
    final private UUID id;
    final private VerdictValue value;
    final private LocalDate verdictDate;
    final private Integer numberOfJurors;
    final private Integer numberOfSplitJurors;
    final private Boolean unanimous;

    @JsonCreator
    public Verdict(@JsonProperty("id") final UUID id,
                   @JsonProperty("value") final VerdictValue value,
                   @JsonProperty("verdictDate") final LocalDate verdictDate,
                   @JsonProperty("numberOfJurors") final Integer numberOfJurors,
                   @JsonProperty("numberOfSplitJurors") final Integer numberOfSplitJurors,
                   @JsonProperty("unanimous") final Boolean unanimous) {
        this.id = id;
        this.value = value;
        this.verdictDate = verdictDate;
        this.numberOfJurors = numberOfJurors;
        this.numberOfSplitJurors = numberOfSplitJurors;
        this.unanimous = unanimous;
    }

    public UUID getId() {
        return id;
    }

    public VerdictValue getValue() {
        return value;
    }

    public LocalDate getVerdictDate() {
        return verdictDate;
    }

    public Integer getNumberOfJurors() {
        return numberOfJurors;
    }

    public Integer getNumberOfSplitJurors() {
        return numberOfSplitJurors;
    }

    public Boolean getUnanimous() {
        return unanimous;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Verdict)) {
            return false;
        }
        Verdict verdict = (Verdict) o;
        return Objects.equals(getId(), verdict.getId()) &&
                Objects.equals(getValue(), verdict.getValue()) &&
                Objects.equals(getVerdictDate(), verdict.getVerdictDate()) &&
                Objects.equals(getNumberOfJurors(), verdict.getNumberOfJurors()) &&
                Objects.equals(getNumberOfSplitJurors(), verdict.getNumberOfSplitJurors()) &&
                Objects.equals(getUnanimous(), verdict.getUnanimous());

    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getValue(), getVerdictDate(), getNumberOfJurors(), getNumberOfSplitJurors(), getUnanimous());
    }

    public static class Builder {

        private UUID id;
        private VerdictValue.Builder value;
        private LocalDate verdictDate;
        private Integer numberOfJurors;
        private Integer numberOfSplitJurors;
        private Boolean unanimous;

        private Builder() {

        }

        public UUID getId() {
            return id;
        }

        public VerdictValue.Builder getValue() {
            return value;
        }

        public LocalDate getVerdictDate() {
            return verdictDate;
        }

        public Integer getNumberOfJurors() {
            return numberOfJurors;
        }

        public Integer getNumberOfSplitJurors() {
            return numberOfSplitJurors;
        }

        public Boolean getUnanimous() {
            return unanimous;
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withValue(VerdictValue.Builder value) {
            this.value = value;
            return this;
        }

        public Builder withVerdictDate(LocalDate verdictDate) {
            this.verdictDate = verdictDate;
            return this;
        }

        public Builder withNumberOfJurors(Integer numberOfJurors) {
            this.numberOfJurors = numberOfJurors;
            return this;
        }

        public Builder withNumberOfSplitJurors(Integer numberOfSplitJurors) {
            this.numberOfSplitJurors = numberOfSplitJurors;
            return this;
        }

        public Builder withUnanimous(Boolean unanimous) {
            this.unanimous = unanimous;
            return this;
        }

        public Verdict build() {
            return new Verdict(this.id, this.value.build(), this.verdictDate, this.numberOfJurors, this.numberOfSplitJurors, this.unanimous);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(Verdict verdict) {
        return builder()
                .withId(verdict.getId())
                .withValue(VerdictValue.from(verdict.getValue()))
                .withVerdictDate(verdict.getVerdictDate())
                .withNumberOfJurors(verdict.getNumberOfJurors())
                .withNumberOfSplitJurors(verdict.getNumberOfSplitJurors())
                .withUnanimous(verdict.getUnanimous());
    }
}
