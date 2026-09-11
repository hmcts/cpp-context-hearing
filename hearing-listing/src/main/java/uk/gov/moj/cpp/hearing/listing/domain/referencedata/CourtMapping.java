package uk.gov.moj.cpp.hearing.listing.domain.referencedata;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("pmd:BeanMembersShouldSerialize")
public class CourtMapping {

    private UUID id;

    private String oucode;

    private String crestCourtId;

    private String crestCourtSiteId;

    private String crestCourtSiteName;

    private String welshCrestCourtSiteName;

    private LocalDate validFrom;

    private LocalDate validTo;

    private String crestCourtName;

    private String welshCrestCourtName;

    private String crestCourtShortName;

    private String welshCrestCourtShortName;

    private String crestCourtFullName;

    private String welshCrestCourtFullName;

    private String crestCourtSiteCode;

    private String courtType;

    @JsonCreator
    public CourtMapping(@JsonProperty("id") final UUID id,
                        @JsonProperty("oucode") final String oucode,
                        @JsonProperty("crestCourtId") final String crestCourtId,
                        @JsonProperty("crestCourtSiteId") final String crestCourtSiteId,
                        @JsonProperty("crestCourtSiteName") final String crestCourtSiteName,
                        @JsonProperty("welshCrestCourtSiteName") final String welshCrestCourtSiteName,
                        @JsonProperty("validFrom") final LocalDate validFrom,
                        @JsonProperty("validTo") final LocalDate validTo,
                        @JsonProperty("crestCourtName") final String crestCourtName,
                        @JsonProperty("welshCrestCourtName") final String welshCrestCourtName,
                        @JsonProperty("crestCourtShortName") final String crestCourtShortName,
                        @JsonProperty("welshCrestCourtShortName") final String welshCrestCourtShortName,
                        @JsonProperty("crestCourtFullName") final String crestCourtFullName,
                        @JsonProperty("welshCrestCourtFullName") final String welshCrestCourtFullName,
                        @JsonProperty("crestCourtSiteCode") final String crestCourtSiteCode,
                        @JsonProperty("courtType") final String courtType) {
        this.id = id;
        this.oucode = oucode;
        this.crestCourtId = crestCourtId;
        this.crestCourtSiteId = crestCourtSiteId;
        this.crestCourtSiteName = crestCourtSiteName;
        this.welshCrestCourtSiteName = welshCrestCourtSiteName;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.crestCourtName = crestCourtName;
        this.welshCrestCourtName = welshCrestCourtName;
        this.crestCourtShortName = crestCourtShortName;
        this.welshCrestCourtShortName = welshCrestCourtShortName;
        this.crestCourtFullName = crestCourtFullName;
        this.welshCrestCourtFullName = welshCrestCourtFullName;
        this.crestCourtSiteCode = crestCourtSiteCode;
        this.courtType = courtType;
    }

    private CourtMapping() {
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

    public String getWelshCrestCourtSiteName() {
        return welshCrestCourtSiteName;
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

    public String getWelshCrestCourtName() {
        return welshCrestCourtName;
    }

    public String getCrestCourtShortName() {
        return crestCourtShortName;
    }

    public String getWelshCrestCourtShortName() {
        return welshCrestCourtShortName;
    }

    public String getCrestCourtFullName() {
        return crestCourtFullName;
    }

    public String getWelshCrestCourtFullName() {
        return welshCrestCourtFullName;
    }

    public String getCrestCourtSiteCode() {
        return crestCourtSiteCode;
    }

    public String getCourtType() {
        return courtType;
    }

    public static class Builder {
        private final CourtMapping instance = new CourtMapping();

        public CourtMapping.Builder withId(final UUID id) {
            instance.id = id;
            return this;
        }

        public CourtMapping.Builder withOucode(final String oucode) {
            instance.oucode = oucode;
            return this;
        }

        public CourtMapping.Builder withCrestCourtId(final String crestCourtId) {
            instance.crestCourtId = crestCourtId;
            return this;
        }

        public CourtMapping.Builder withCrestCourtSiteId(final String crestCourtSiteId) {
            instance.crestCourtSiteId = crestCourtSiteId;
            return this;
        }

        public CourtMapping.Builder withCrestCourtName(final String crestCourtName) {
            instance.crestCourtName = crestCourtName;
            return this;
        }

        public CourtMapping.Builder withWelshCrestCourtName(final String welshCrestCourtName) {
            instance.welshCrestCourtName = welshCrestCourtName;
            return this;
        }

        public CourtMapping.Builder withCrestCourtSiteName(final String crestCourtSiteName) {
            instance.crestCourtSiteName = crestCourtSiteName;
            return this;
        }

        public CourtMapping.Builder withWelshCrestCourtSiteName(final String welshCrestCourtSiteName) {
            instance.welshCrestCourtSiteName = welshCrestCourtSiteName;
            return this;
        }

        public CourtMapping.Builder withCrestCourtShortName(final String crestCourtShortName) {
            instance.crestCourtShortName = crestCourtShortName;
            return this;
        }

        public CourtMapping.Builder withWelshCrestCourtShortName(final String welshCrestCourtShortName) {
            instance.welshCrestCourtShortName = welshCrestCourtShortName;
            return this;
        }

        public CourtMapping.Builder withCrestCourtFullName(final String crestCourtFullName) {
            instance.crestCourtFullName = crestCourtFullName;
            return this;
        }

        public CourtMapping.Builder withWelshCrestCourtFullName(final String welshCrestCourtFullName) {
            instance.welshCrestCourtFullName = welshCrestCourtFullName;
            return this;
        }

        public CourtMapping.Builder withValidFrom(final LocalDate validFrom) {
            instance.validFrom = validFrom;
            return this;
        }

        public CourtMapping.Builder withValidTo(final LocalDate validTo) {
            instance.validTo = validTo;
            return this;
        }

        public CourtMapping.Builder withCrestCourtSiteCode(final String crestCourtSiteCode) {
            instance.crestCourtSiteCode = crestCourtSiteCode;
            return this;
        }

        public CourtMapping.Builder withCourtType(final String courtType) {
            instance.courtType = courtType;
            return this;
        }

        public CourtMapping build() {
            return instance;
        }
    }
}
