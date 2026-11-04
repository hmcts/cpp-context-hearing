package uk.gov.moj.cpp.hearing.listing.domain.referencedata;

import java.time.LocalDate;
import java.util.UUID;

@SuppressWarnings("pmd:BeanMembersShouldSerialize")
public class CourtMapping {

    private UUID id;

    private String oucode;

    private String crestCourtId;

    private String crestCourtSiteId;

    private String crestCourtSiteName;

    private LocalDate validFrom;

    private LocalDate validTo;

    private String crestCourtName;

    private String crestCourtShortName;

    private String crestCourtFullName;

    private String crestCourtSiteCode;

    private String courtType;

    public CourtMapping(final UUID id,
                        final String oucode,
                        final String crestCourtId,
                        final String crestCourtSiteId,
                        final String crestCourtSiteName,
                        final LocalDate validFrom,
                        final LocalDate validTo,
                        final String crestCourtName,
                        final String crestCourtShortName,
                        final String crestCourtFullName,
                        final String crestCourtSiteCode,
                        final String courtType) {
        this.id = id;
        this.oucode = oucode;
        this.crestCourtId = crestCourtId;
        this.crestCourtSiteId = crestCourtSiteId;
        this.crestCourtSiteName = crestCourtSiteName;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.crestCourtName = crestCourtName;
        this.crestCourtShortName = crestCourtShortName;
        this.crestCourtFullName = crestCourtFullName;
        this.crestCourtSiteCode = crestCourtSiteCode;
        this.courtType = courtType;
    }

    public UUID getId() {
        return id;
    }

    public String getOucode() {
        return oucode;
    }

    public String getCrestCourtId() {
        return crestCourtId;
    }

    public String getCrestCourtSiteId() {
        return crestCourtSiteId;
    }

    public String getCrestCourtSiteName() {
        return crestCourtSiteName;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public String getCrestCourtName() {
        return crestCourtName;
    }

    public String getCrestCourtShortName() {
        return crestCourtShortName;
    }

    public String getCrestCourtFullName() {
        return crestCourtFullName;
    }

    public String getCrestCourtSiteCode() {
        return crestCourtSiteCode;
    }

    public String getCourtType() {
        return courtType;
    }

    public static class Builder {
        private UUID id;
        private String oucode;
        private String crestCourtId;
        private String crestCourtSiteId;
        private String crestCourtSiteName;
        private LocalDate validFrom;
        private LocalDate validTo;
        private String crestCourtName;
        private String crestCourtShortName;
        private String crestCourtFullName;
        private String crestCourtSiteCode;
        private String courtType;

        public CourtMapping.Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public CourtMapping.Builder withOucode(final String oucode) {
            this.oucode = oucode;
            return this;
        }

        public CourtMapping.Builder withCrestCourtId(final String crestCourtId) {
            this.crestCourtId = crestCourtId;
            return this;
        }

        public CourtMapping.Builder withCrestCourtSiteId(final String crestCourtSiteId) {
            this.crestCourtSiteId = crestCourtSiteId;
            return this;
        }

        public CourtMapping.Builder withCrestCourtName(final String crestCourtName) {
            this.crestCourtName = crestCourtName;
            return this;
        }

        public CourtMapping.Builder withCrestCourtSiteName(final String crestCourtSiteName) {
            this.crestCourtSiteName = crestCourtSiteName;
            return this;
        }

        public CourtMapping.Builder withCrestCourtShortName(final String crestCourtShortName) {
            this.crestCourtShortName = crestCourtShortName;
            return this;
        }

        public CourtMapping.Builder withValidFrom(final LocalDate validFrom) {
            this.validFrom = validFrom;
            return this;
        }

        public CourtMapping.Builder withValidTo(final LocalDate validTo) {
            this.validTo = validTo;
            return this;
        }

        public CourtMapping.Builder withCrestCourtSiteCode(final String crestCourtSiteCode) {
            this.crestCourtSiteCode = crestCourtSiteCode;
            return this;
        }

        public CourtMapping.Builder withCourtType(final String courtType) {
            this.courtType = courtType;
            return this;
        }

        public CourtMapping build() {
            return new CourtMapping(id, oucode, crestCourtId, crestCourtSiteId, crestCourtSiteName, validFrom, validTo, crestCourtName, crestCourtShortName, crestCourtFullName, crestCourtSiteCode, courtType);
        }
    }
}
