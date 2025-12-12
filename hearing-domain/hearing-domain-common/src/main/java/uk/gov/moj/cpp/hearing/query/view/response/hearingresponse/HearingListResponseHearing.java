package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JurisdictionType;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("hearing")
public class HearingListResponseHearing {

    private final UUID id;
    private final HearingType type;
    private final JurisdictionType jurisdictionType;
    private final String reportingRestrictionReason;
    private final String hearingLanguage;
    private final List<HearingDay> hearingDays;
    private final List<ProsecutionCase> prosecutionCases;
    private final boolean hasSharedResults;

    @JsonCreator
    public HearingListResponseHearing(@JsonProperty("id") UUID id,
                                      @JsonProperty("type") HearingType type,
                                      @JsonProperty("jurisdictionType") JurisdictionType jurisdictionType,
                                      @JsonProperty("reportingRestrictionReason") String reportingRestrictionReason,
                                      @JsonProperty("hearingLanguage") String hearingLanguage,
                                      @JsonProperty("hearingDays") List<HearingDay> hearingDays,
                                      @JsonProperty("prosecutionCases") List<ProsecutionCase> prosecutionCases,
                                      @JsonProperty("hasSharedResults") boolean hasSharedResults) {
        super();
        this.id = id;
        this.type = type;
        this.jurisdictionType = jurisdictionType;
        this.reportingRestrictionReason = reportingRestrictionReason;
        this.hearingLanguage = hearingLanguage;
        this.hearingDays = hearingDays;
        this.prosecutionCases = prosecutionCases;
        this.hasSharedResults = hasSharedResults;
    }

    private HearingListResponseHearing(final Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.jurisdictionType = builder.jurisdictionType;
        this.reportingRestrictionReason = builder.reportingRestrictionReason;
        this.hearingLanguage = builder.hearingLanguage;
        this.hearingDays = builder.hearingDays;
        this.prosecutionCases = builder.prosecutionCases;
        this.hasSharedResults = builder.hasSharedResults;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public HearingType getType() {
        return type;
    }

    public JurisdictionType getJurisdictionType() {
        return jurisdictionType;
    }

    public String getReportingRestrictionReason() {
        return reportingRestrictionReason;
    }

    public String getHearingLanguage() {
        return hearingLanguage;
    }

    public List<HearingDay> getHearingDays() {
        return hearingDays;
    }

    public List<ProsecutionCase> getProsecutionCases() {
        return prosecutionCases;
    }

    public boolean isHasSharedResults() {
        return hasSharedResults;
    }

    public static class Builder {

        private UUID id;
        private HearingType type;
        private JurisdictionType jurisdictionType;
        private String reportingRestrictionReason;
        private String hearingLanguage;
        private List<HearingDay> hearingDays;
        private List<ProsecutionCase> prosecutionCases;
        private boolean hasSharedResults;

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withType(final HearingType type) {
            this.type = type;
            return this;
        }

        public Builder withJurisdictionType(final JurisdictionType jurisdictionType) {
            this.jurisdictionType = jurisdictionType;
            return this;
        }

        public Builder withReportingRestrictionReason(final String reportingRestrictionReason) {
            this.reportingRestrictionReason = reportingRestrictionReason;
            return this;
        }

        public Builder withHearingLanguage(final String hearingLanguage) {
            this.hearingLanguage = hearingLanguage;
            return this;
        }

        public Builder withHearingDays(final List<HearingDay> hearingDays) {
            this.hearingDays = hearingDays;
            return this;
        }

        public Builder withProsecutionCases(final List<ProsecutionCase> prosecutionCases) {
            this.prosecutionCases = prosecutionCases;
            return this;
        }

        public Builder withHasSharedResults(final boolean hasSharedResults) {
            this.hasSharedResults = hasSharedResults;
            return this;
        }

        public HearingListResponseHearing build() {
            return new HearingListResponseHearing(this);
        }
    }
}