package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @deprecated (GPE-15454, add-request-for-outstanding-fines command removed with Real-Time Updates feature)
 */
@Deprecated
@Event("hearing.outstanding-fines-requested")
public class OutstandingFinesRequested {

    private LocalDate hearingDate;

    public OutstandingFinesRequested() {
    }

    @JsonCreator
    public OutstandingFinesRequested(
            @JsonProperty(value = "hearingDate", required = true) final LocalDate hearingDate) {
        this.hearingDate = hearingDate;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final OutstandingFinesRequested copy) {
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

        public OutstandingFinesRequested build() {
            return new OutstandingFinesRequested(hearingDate);
        }
    }
}
