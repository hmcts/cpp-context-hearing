package uk.gov.moj.cpp.hearing.eventlog;

import uk.gov.justice.core.courts.JurisdictionType;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"squid:S2384"})
public class PublicHearingInitiated {

    private UUID hearingId;

    private List<UUID> cases;

    private ZonedDateTime hearingDateTime;

    private List<HearingCaseDetail> caseDetails;

    private List<HearingApplicationDetail> applicationDetails;

    private JurisdictionType jurisdictionType;


    public static PublicHearingInitiated publicHearingInitiated() {
        return new PublicHearingInitiated();
    }

    public PublicHearingInitiated setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public List<UUID> getCases() {
        return cases;
    }

    public PublicHearingInitiated setCases(final List<UUID> cases) {
        this.cases = cases;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }


    public ZonedDateTime getHearingDateTime() {
        return hearingDateTime;
    }

    public PublicHearingInitiated setHearingDateTime(final ZonedDateTime hearingDateTime) {
        this.hearingDateTime = hearingDateTime;
        return this;
    }


    public List<HearingCaseDetail> getCaseDetails() {
        return caseDetails;
    }

    public PublicHearingInitiated setCaseDetails(final List<HearingCaseDetail> caseDetails) {
        this.caseDetails = caseDetails;
        return this;
    }

    public List<HearingApplicationDetail> getApplicationDetails() {
        return applicationDetails;
    }

    public PublicHearingInitiated setApplicationDetails(final List<HearingApplicationDetail> applicationDetails) {
        this.applicationDetails = applicationDetails;
        return this;
    }

    public JurisdictionType getJurisdictionType() {
        return jurisdictionType;
    }

    public PublicHearingInitiated setJurisdictionType(final JurisdictionType jurisdictionType) {
        this.jurisdictionType = jurisdictionType;
        return this;
    }
}