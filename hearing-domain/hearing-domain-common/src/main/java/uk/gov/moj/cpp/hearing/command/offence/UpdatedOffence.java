package uk.gov.moj.cpp.hearing.command.offence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.UUID;

public final class UpdatedOffence {

    private UUID id;

    private String offenceCode;

    private String wording;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer count;

    private LocalDate convictionDate;

    @JsonCreator
    public UpdatedOffence(@JsonProperty("id") final UUID id,
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

    public UUID getId() {
        return id;
    }

    public String getOffenceCode() {
        return offenceCode;
    }

    public String getWording() {
        return wording;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Integer getCount() {
        return count;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public UpdatedOffence setId(UUID id) {
        this.id = id;
        return this;
    }

    public UpdatedOffence setOffenceCode(String offenceCode) {
        this.offenceCode = offenceCode;
        return this;
    }

    public UpdatedOffence setWording(String wording) {
        this.wording = wording;
        return this;
    }

    public UpdatedOffence setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public UpdatedOffence setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public UpdatedOffence setCount(Integer count) {
        this.count = count;
        return this;
    }

    public UpdatedOffence setConvictionDate(LocalDate convictionDate) {
        this.convictionDate = convictionDate;
        return this;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID id;

        private String offenceCode;

        private String wording;

        private LocalDate startDate;

        private LocalDate endDate;

        private Integer count;

        private LocalDate convictionDate;

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

        public UpdatedOffence build() {
            return new UpdatedOffence(id, offenceCode, wording, startDate, endDate, count, convictionDate);
        }
    }
}
