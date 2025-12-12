package uk.gov.moj.cpp.hearing.command.offence;

import uk.gov.moj.cpp.external.domain.listing.StatementOfOffence;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class DefendantOffence {

    private UUID id;

    private String offenceCode;

    private String wording;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer count;

    private LocalDate convictionDate;

    private StatementOfOffence statementOfOffence;

    public DefendantOffence() {
    }

    @JsonCreator
    public DefendantOffence(@JsonProperty("id") final UUID id,
                            @JsonProperty("offenceCode") final String offenceCode,
                            @JsonProperty("wording") final String wording,
                            @JsonProperty("startDate") final LocalDate startDate,
                            @JsonProperty("endDate") final LocalDate endDate,
                            @JsonProperty("count") final Integer count,
                            @JsonProperty("convictionDate") final LocalDate convictionDate,
                            @JsonProperty("statementOfOffence") final StatementOfOffence statementOfOffence) {

        this.id = id;
        this.offenceCode = offenceCode;
        this.wording = wording;
        this.startDate = startDate;
        this.endDate = endDate;
        this.count = count;
        this.convictionDate = convictionDate;
        this.statementOfOffence = statementOfOffence;
    }

    public static DefendantOffence defendantOffence() {
        return new DefendantOffence();
    }

    public UUID getId() {
        return id;
    }

    public DefendantOffence setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getOffenceCode() {
        return offenceCode;
    }

    public DefendantOffence setOffenceCode(String offenceCode) {
        this.offenceCode = offenceCode;
        return this;
    }

    public String getWording() {
        return wording;
    }

    public DefendantOffence setWording(String wording) {
        this.wording = wording;
        return this;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public DefendantOffence setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public DefendantOffence setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public Integer getCount() {
        return count;
    }

    public DefendantOffence setCount(Integer count) {
        this.count = count;
        return this;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public DefendantOffence setConvictionDate(LocalDate convictionDate) {
        this.convictionDate = convictionDate;
        return this;
    }

    public StatementOfOffence getStatementOfOffence() {
        return statementOfOffence;
    }

    public DefendantOffence setStatementOfOffence(StatementOfOffence statementOfOffence) {
        this.statementOfOffence = statementOfOffence;
        return this;
    }
}
