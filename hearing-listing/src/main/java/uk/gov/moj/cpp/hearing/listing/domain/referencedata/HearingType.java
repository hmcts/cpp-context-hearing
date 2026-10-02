package uk.gov.moj.cpp.hearing.listing.domain.referencedata;

import java.time.LocalDate;
import java.util.UUID;

@SuppressWarnings("pmd:BeanMembersShouldSerialize")
public class HearingType {

    private UUID id;

    private Integer seqId;

    private String hearingCode;

    private String hearingDescription;

    private String welshHearingDescription;

    private Integer defaultDurationMin;

    private LocalDate validFrom;

    private LocalDate validTo;

    private String exhibitHearingCode;

    private String exhibitHearingDescription;

    public HearingType(final UUID id, final Integer seqId, final String hearingCode, final String hearingDescription, final String welshHearingDescription, final Integer defaultDurationMin, final LocalDate validFrom, final LocalDate validTo, final String exhibitHearingCode, final String exhibitHearingDescription) {
        this.id = id;
        this.seqId = seqId;
        this.hearingCode = hearingCode;
        this.hearingDescription = hearingDescription;
        this.welshHearingDescription = welshHearingDescription;
        this.defaultDurationMin = defaultDurationMin;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.exhibitHearingCode = exhibitHearingCode;
        this.exhibitHearingDescription = exhibitHearingDescription;
    }

    public HearingType() {}

    public UUID getId() {
        return id;
    }

    public Integer getSeqId() {
        return seqId;
    }

    public String getHearingCode() {
        return hearingCode;
    }

    public String getHearingDescription() {
        return hearingDescription;
    }

    public String getWelshHearingDescription() {
        return welshHearingDescription;
    }

    public Integer getDefaultDurationMin() {
        return defaultDurationMin;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public String getExhibitHearingCode() {
        return exhibitHearingCode;
    }

    public String getExhibitHearingDescription() {
        return exhibitHearingDescription;
    }

    public static class Builder {
        private UUID id;

        private Integer seqId;

        private String hearingCode;

        private String hearingDescription;

        private String welshHearingDescription;

        private Integer defaultDurationMin;

        private LocalDate validFrom;

        private LocalDate validTo;

        private String exhibitHearingCode;

        private String exhibitHearingDescription;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withExhibitHearingCode(String exhibitHearingCode) {
            this.exhibitHearingCode = exhibitHearingCode;
            return this;
        }

        public Builder withExhibitHearingDescription(String exhibitHearingDescription) {
            this.exhibitHearingDescription = exhibitHearingDescription;
            return this;
        }

        public Builder withHearingCode(String hearingCode) {
            this.hearingCode = hearingCode;
            return this;
        }

        public Builder withHearingDescription(String hearingDescription) {
            this.hearingDescription = hearingDescription;
            return this;
        }

        public HearingType build() {
            return new HearingType(id, seqId, hearingCode, hearingDescription, welshHearingDescription, defaultDurationMin, validFrom, validTo, exhibitHearingCode, exhibitHearingDescription);
        }
    }
}
