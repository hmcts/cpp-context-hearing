package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.offence-updated")
@SuppressWarnings("squid:S00107")
public class OffenceUpdated {

    private final UUID id;

    private final UUID hearingId;

    private final String offenceCode;

    private final String wording;

    private final LocalDate startDate;

    private final LocalDate endDate;

    private final Integer count;

    private final LocalDate convictionDate;

    private OffenceUpdated(@JsonProperty("id") final UUID id,
                           @JsonProperty("hearingId") final UUID hearingId,
                           @JsonProperty("offenceCode") final String offenceCode,
                           @JsonProperty("wording") final String wording,
                           @JsonProperty("startDate") final LocalDate startDate,
                           @JsonProperty("endDate") final LocalDate endDate,
                           @JsonProperty("count") final Integer count,
                           @JsonProperty("convictionDate") final LocalDate convictionDate) {
        this.id = id;
        this.hearingId = hearingId;
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

    public UUID getHearingId() {
        return hearingId;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID id;

        private UUID hearingId;

        private String offenceCode;

        private String wording;

        private LocalDate startDate;

        private LocalDate endDate;

        private Integer count;

        private LocalDate convictionDate;

        private Builder() {
        }

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
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

        public OffenceUpdated build() {
            return new OffenceUpdated(id, hearingId, offenceCode, wording, startDate, endDate, count, convictionDate);
        }

    }
}
