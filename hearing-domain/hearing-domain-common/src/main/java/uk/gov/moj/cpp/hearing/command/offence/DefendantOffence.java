package uk.gov.moj.cpp.hearing.command.offence;

import uk.gov.moj.cpp.external.domain.listing.StatementOfOffence;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class DefendantOffence extends BaseDefendantOffence {

    private StatementOfOffence statementOfOffence;

    @JsonCreator
    public DefendantOffence(@JsonProperty("id") final UUID id,
                            @JsonProperty("offenceCode") final String offenceCode,
                            @JsonProperty("wording") final String wording,
                            @JsonProperty("startDate") final LocalDate startDate,
                            @JsonProperty("endDate") final LocalDate endDate,
                            @JsonProperty("count") final Integer count,
                            @JsonProperty("convictionDate") final LocalDate convictionDate,
                            @JsonProperty("statementOfOffence") final StatementOfOffence statementOfOffence) {
        super(id, offenceCode, wording, startDate, endDate, count, convictionDate);
        this.statementOfOffence = statementOfOffence;
    }

    public StatementOfOffence getStatementOfOffence() {
        return statementOfOffence;
    }


    public static DefendantOffenceBuilder builder(final UUID id, final String offenceCode, final String wording, final LocalDate startDate, final LocalDate endDate, final Integer count, final LocalDate convictionDate) {
        return new DefendantOffenceBuilder(id, offenceCode, wording,startDate, endDate, count, convictionDate);
    }

    public static class DefendantOffenceBuilder extends BaseDefendantOffence.Builder {

        private StatementOfOffence statementOfOffence;

        DefendantOffenceBuilder(final UUID id, final String offenceCode, final String wording, final LocalDate startDate, final LocalDate endDate, final Integer count, final LocalDate convictionDate) {
            this.id = id;
            this.offenceCode = offenceCode;
            this.wording = wording;
            this.startDate = startDate;
            this.endDate = endDate;
            this.count = count;
            this.convictionDate = convictionDate;
        }


        public DefendantOffenceBuilder withStatementOfOffence(final StatementOfOffence statementOfOffence) {
            this.statementOfOffence = statementOfOffence;
            return this;
        }

        @Override
        public DefendantOffence build() {
            return new DefendantOffence(id, offenceCode, wording, startDate, endDate, count, convictionDate, statementOfOffence);
        }
    }
}
