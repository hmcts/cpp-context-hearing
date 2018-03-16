package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.UUID;

import static java.util.Optional.ofNullable;

public class Offence {

    private final UUID id;
    private final UUID caseId;
    private final String offenceCode;
    private final String wording;
    private final String section;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer orderIndex;
    private final Integer count;
    private final LocalDate convictionDate;
    private Plea plea;

    @JsonCreator
    public Offence(@JsonProperty("id") final UUID id,
                   @JsonProperty("caseId") final UUID caseId,
                   @JsonProperty("offenceCode") final String offenceCode,
                   @JsonProperty("wording") final String wording,
                   @JsonProperty("section") final String section,
                   @JsonProperty("startDate") final LocalDate startDate,
                   @JsonProperty("endDate") final LocalDate endDate,
                   @JsonProperty("orderIndex") final Integer orderIndex,
                   @JsonProperty("count") final Integer count,
                   @JsonProperty("convictionDate") final LocalDate convictionDate,
                   @JsonProperty("plea") final Plea plea) {
        this.id = id;
        this.caseId = caseId;
        this.offenceCode = offenceCode;
        this.wording = wording;
        this.section = section;
        this.startDate = startDate;
        this.endDate = endDate;
        this.orderIndex = orderIndex;
        this.count = count;
        this.convictionDate = convictionDate;
        this.plea = plea;
    }

    public UUID getId() {
        return id;
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

    public String getSection() {
        return section;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public Integer getCount() {
        return count;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public Plea getPlea() {
        return plea;
    }

    public void setPlea(Plea plea) {
        this.plea = plea;
    }

    public static class Builder {

        private UUID id;
        private String offenceCode;
        private String wording;
        private String section;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer orderIndex;
        private Integer count;
        private LocalDate convictionDate;
        private Plea.Builder plea;
        private UUID caseId;

        private Builder() {

        }

        public UUID getId() {
            return id;
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

        public String getSection() {
            return section;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public Integer getOrderIndex() {
            return orderIndex;
        }

        public Integer getCount() {
            return count;
        }

        public LocalDate getConvictionDate() {
            return convictionDate;
        }

        public Plea.Builder getPlea() {
            return plea;
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withCaseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withOffenceCode(String offenceCode) {
            this.offenceCode = offenceCode;
            return this;
        }

        public Builder withWording(String wording) {
            this.wording = wording;
            return this;
        }

        public Builder withSection(String section) {
            this.section = section;
            return this;
        }

        public Builder withStartDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder withEndDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder withOrderIndex(Integer orderIndex) {
            this.orderIndex = orderIndex;
            return this;
        }

        public Builder withCount(Integer count) {
            this.count = count;
            return this;
        }

        public Builder withConvictionDate(LocalDate convictionDate) {
            this.convictionDate = convictionDate;
            return this;
        }

        public Builder withPlea(Plea.Builder plea) {
            this.plea = plea;
            return this;
        }

        public Offence build() {
            return new Offence(id, caseId, offenceCode, wording, section, startDate, endDate, orderIndex, count, convictionDate,
                    ofNullable(plea).map(Plea.Builder::build).orElse(null)
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(Offence offence) {

        return builder()
                .withId(offence.getId())
                .withCaseId(offence.getCaseId())
                .withOffenceCode(offence.getOffenceCode())
                .withWording(offence.getWording())
                .withSection(offence.getSection())
                .withStartDate(offence.getStartDate())
                .withEndDate(offence.getEndDate())
                .withOrderIndex(offence.getOrderIndex())
                .withCount(offence.getCount())
                .withConvictionDate(offence.getConvictionDate());
    }
}
