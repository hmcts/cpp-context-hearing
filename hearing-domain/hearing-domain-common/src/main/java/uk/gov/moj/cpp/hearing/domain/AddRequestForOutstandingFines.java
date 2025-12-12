package uk.gov.moj.cpp.hearing.domain;

import java.time.LocalDate;

public class AddRequestForOutstandingFines {

    private LocalDate hearingDate;

    public AddRequestForOutstandingFines() {
    }

    private AddRequestForOutstandingFines(final LocalDate hearingDate) {
        this.hearingDate = hearingDate;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final AddRequestForOutstandingFines copy) {
        Builder builder = new Builder();
        builder.hearingDate = copy.getHearingDate();
        return builder;
    }

    public LocalDate getHearingDate() {
        return hearingDate;
    }

    public static final class Builder {
        private LocalDate hearingDate;

        private Builder() {
        }

        public Builder withHearingDate(final LocalDate hearingDate) {
            this.hearingDate = hearingDate;
            return this;
        }

        public AddRequestForOutstandingFines build() {
            return new AddRequestForOutstandingFines(hearingDate);
        }
    }
}
