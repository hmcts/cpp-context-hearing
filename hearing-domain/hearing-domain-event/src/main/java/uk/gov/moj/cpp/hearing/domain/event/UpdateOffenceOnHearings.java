package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Event("hearing.update-case-defendant-offence-enriched-with-hearing-ids")
@SuppressWarnings("squid:S00107")
public class UpdateOffenceOnHearings {

    private final UUID id;

    private final String offenceCode;

    private final String wording;

    private final LocalDate startDate;

    private final LocalDate endDate;

    private final Integer count;

    private final LocalDate convictionDate;

    private final List<UUID> hearingIds;

    private UpdateOffenceOnHearings(@JsonProperty("id") final UUID id,
                                    @JsonProperty("offenceCode") final String offenceCode,
                                    @JsonProperty("wording") final String wording,
                                    @JsonProperty("startDate") final LocalDate startDate,
                                    @JsonProperty("endDate") final LocalDate endDate,
                                    @JsonProperty("count") final Integer count,
                                    @JsonProperty("convictionDate") final LocalDate convictionDate,
                                    @JsonProperty("hearingIds") final List<UUID> hearingIds) {
        this.id = id;
        this.offenceCode = offenceCode;
        this.wording = wording;
        this.startDate = startDate;
        this.endDate = endDate;
        this.count = count;
        this.convictionDate = convictionDate;
        this.hearingIds = new ArrayList<>(hearingIds);
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

    public List<UUID> getHearingIds() {
        return new ArrayList<>(hearingIds);
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

        private List<UUID> hearingIds;

        private Builder() {
        }

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withHearingIds(final List<UUID> hearingIds) {
            this.hearingIds = new ArrayList<>(hearingIds);
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

        public UpdateOffenceOnHearings build() {
            return new UpdateOffenceOnHearings(id, offenceCode, wording, startDate, endDate, count, convictionDate, hearingIds);
        }

    }
}
