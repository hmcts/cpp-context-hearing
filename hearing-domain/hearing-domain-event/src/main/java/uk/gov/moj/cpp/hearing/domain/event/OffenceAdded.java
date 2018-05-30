package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.offence-added")
@SuppressWarnings("squid:S00107")
public class OffenceAdded implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;

    private final UUID hearingId;

    private final UUID defendantId;

    private final UUID caseId;

    private final String offenceCode;

    private final String wording;

    private final LocalDate startDate;

    private final LocalDate endDate;

    private final Integer count;

    private final LocalDate convictionDate;

    private OffenceAdded(@JsonProperty("id") final UUID id,
                         @JsonProperty("hearingId") final UUID hearingId,
                         @JsonProperty("defendantId") final UUID defendantId,
                         @JsonProperty("caseId") final UUID caseId,
                         @JsonProperty("offenceCode") final String offenceCode,
                         @JsonProperty("wording") final String wording,
                         @JsonProperty("startDate") final LocalDate startDate,
                         @JsonProperty("endDate") final LocalDate endDate,
                         @JsonProperty("count") final Integer count,
                         @JsonProperty("convictionDate") final LocalDate convictionDate) {
        this.id = id;
        this.hearingId = hearingId;
        this.defendantId = defendantId;
        this.caseId = caseId;
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

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getCaseId() {
        return caseId;
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

        private UUID defendantId;

        private UUID caseId;

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

        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
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

        public OffenceAdded build() {
            return new OffenceAdded(id, hearingId, defendantId, caseId, offenceCode, wording, startDate, endDate, count, convictionDate);
        }

    }
}
