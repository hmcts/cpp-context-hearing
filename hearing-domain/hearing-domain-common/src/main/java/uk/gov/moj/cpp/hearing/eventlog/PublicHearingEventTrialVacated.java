package uk.gov.moj.cpp.hearing.eventlog;

import uk.gov.justice.core.courts.JurisdictionType;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"squid:S2384"})
public class PublicHearingEventTrialVacated {

    private UUID hearingId;

    private UUID vacatedTrialReasonId;

    private Boolean hasInterpreter;

    private ZonedDateTime hearingDateTime;

    private List<HearingCaseDetail> caseDetails;

    private List<HearingApplicationDetail> applicationDetails;

    private JurisdictionType jurisdictionType;


    public static PublicHearingEventTrialVacated publicHearingEventTrialVacated() {
        return new PublicHearingEventTrialVacated();
    }

    public PublicHearingEventTrialVacated setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public PublicHearingEventTrialVacated setVacatedTrialReasonId(final UUID vacatedTrialReasonId) {
        this.vacatedTrialReasonId = vacatedTrialReasonId;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getVacatedTrialReasonId() {
        return vacatedTrialReasonId;
    }

    public ZonedDateTime getHearingDateTime() {
        return hearingDateTime;
    }

    public PublicHearingEventTrialVacated setHearingDateTime(final ZonedDateTime hearingDateTime) {
        this.hearingDateTime = hearingDateTime;
        return this;
    }


    public List<HearingCaseDetail> getCaseDetails() {
        return caseDetails;
    }

    public PublicHearingEventTrialVacated setCaseDetails(final List<HearingCaseDetail> caseDetails) {
        this.caseDetails = caseDetails;
        return this;
    }

    public Boolean getHasInterpreter() {
        return hasInterpreter;
    }

    public PublicHearingEventTrialVacated setHasInterpreter(final Boolean hasInterpreter) {
        this.hasInterpreter = hasInterpreter;
        return this;
    }

    public List<HearingApplicationDetail> getApplicationDetails() {
        return applicationDetails;
    }

    public PublicHearingEventTrialVacated setApplicationDetails(final List<HearingApplicationDetail> applicationDetails) {
        this.applicationDetails = applicationDetails;
        return this;
    }

    public JurisdictionType getJurisdictionType() {
        return jurisdictionType;
    }

    public PublicHearingEventTrialVacated setJurisdictionType(final JurisdictionType jurisdictionType) {
        this.jurisdictionType = jurisdictionType;
        return this;
    }
}