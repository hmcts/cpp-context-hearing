package uk.gov.moj.cpp.hearing.command.offence;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BaseDefendantOffence {

    private UUID id;

    private String offenceCode;

    private String wording;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer count;

    private LocalDate convictionDate;

    @JsonCreator
    public BaseDefendantOffence(@JsonProperty("id") final UUID id,
                                @JsonProperty("offenceCode") final String offenceCode,
                                @JsonProperty("wording") final String wording,
                                @JsonProperty("startDate") final LocalDate startDate,
                                @JsonProperty("endDate") final LocalDate endDate,
                                @JsonProperty("count") final Integer count,
                                @JsonProperty("convictionDate") final LocalDate convictionDate) {
        this.id = id;
        this.offenceCode = offenceCode;
        this.wording = wording;
        this.startDate = startDate;
        this.endDate = endDate;
        this.count = count;
        this.convictionDate = convictionDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public BaseDefendantOffence setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getOffenceCode() {
        return offenceCode;
    }

    public BaseDefendantOffence setOffenceCode(String offenceCode) {
        this.offenceCode = offenceCode;
        return this;
    }

    public String getWording() {
        return wording;
    }

    public BaseDefendantOffence setWording(String wording) {
        this.wording = wording;
        return this;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public BaseDefendantOffence setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public BaseDefendantOffence setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public Integer getCount() {
        return count;
    }

    public BaseDefendantOffence setCount(Integer count) {
        this.count = count;
        return this;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public BaseDefendantOffence setConvictionDate(LocalDate convictionDate) {
        this.convictionDate = convictionDate;
        return this;
    }

    public static class Builder {

        protected UUID id;

        protected String offenceCode;

        protected String wording;

        protected LocalDate startDate;

        protected LocalDate endDate;

        protected Integer count;

        protected LocalDate convictionDate;

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withOffenceCode(final String offenceCode) {
            this.offenceCode = offenceCode;
            return this;
        }

        public Builder withWording(final String wording) {
            this.wording = wording;
            return this;
        }

        public Builder withStartDate(final LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder withEndDate(final LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder withCount(final Integer count) {
            this.count = count;
            return this;
        }

        public Builder withConvictionDate(final LocalDate convictionDate) {
            this.convictionDate = convictionDate;
            return this;
        }

        public BaseDefendantOffence build() {
            return new BaseDefendantOffence(id, offenceCode, wording, startDate, endDate, count, convictionDate);
        }
    }
}
