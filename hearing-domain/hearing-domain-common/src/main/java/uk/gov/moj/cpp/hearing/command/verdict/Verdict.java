package uk.gov.moj.cpp.hearing.command.verdict;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Verdict implements Serializable {
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
}
