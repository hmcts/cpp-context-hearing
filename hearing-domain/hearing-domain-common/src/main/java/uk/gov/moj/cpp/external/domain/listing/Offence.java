package uk.gov.moj.cpp.external.domain.listing;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = Include.NON_NULL)
@SuppressWarnings("squid:S1067")
public class Offence implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String id;
    private final String offenceCode;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final StatementOfOffence statementOfOffence;

    @JsonCreator
    public Offence(@JsonProperty(value = "id") final String id,
                   @JsonProperty(value = "offenceCode") final String offenceCode,
                   @JsonProperty(value = "startDate") final LocalDate startDate,
                   @JsonProperty(value = "endDate") final LocalDate endDate,
                   @JsonProperty(value = "statementOfOffence") final StatementOfOffence statementOfOffence) {
        this.id = id;
        this.offenceCode = offenceCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.statementOfOffence = statementOfOffence;
    }

    public String getId() {
        return id;
    }

    public String getOffenceCode() {
        return offenceCode;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public StatementOfOffence getStatementOfOffence() {
        return statementOfOffence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Offence offence = (Offence) o;
        return Objects.equals(id, offence.id) &&
                Objects.equals(offenceCode, offence.offenceCode) &&
                Objects.equals(startDate, offence.startDate) &&
                Objects.equals(endDate, offence.endDate) &&
                Objects.equals(statementOfOffence, offence.statementOfOffence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, offenceCode, startDate, endDate, statementOfOffence);
    }
}
