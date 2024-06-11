package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.eventlog.HearingApplicationDetail;
import uk.gov.moj.cpp.hearing.eventlog.HearingCaseDetail;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.trial-vacated")
@SuppressWarnings({"squid:S2384"})
public class HearingTrialVacated implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private UUID vacatedTrialReasonId;
    private String code;
    private String description;
    private String type;
    private UUID courtCentreId;
    private Boolean hasInterpreter;
    private ZonedDateTime hearingDay;
    private JurisdictionType jurisdictionType;
    private List<HearingCaseDetail> caseDetails;
    private List<HearingApplicationDetail> applicationDetails;

    @JsonCreator
    public HearingTrialVacated(@JsonProperty("hearingId") final UUID hearingId,
                               @JsonProperty("vacatedTrialReasonId") final UUID vacatedTrialReasonId,
                               @JsonProperty("code") final String code,
                               @JsonProperty("type") final String type,
                               @JsonProperty("description") final String description,
                               @JsonProperty("courtCentreId") final UUID courtCentreId,
                               @JsonProperty("hasInterpreter") final Boolean hasInterpreter,
                               @JsonProperty("hearingDay") final ZonedDateTime hearingDay,
                               @JsonProperty("caseDetails") final List<HearingCaseDetail> caseDetails,
                               @JsonProperty("applicationDetails") final List<HearingApplicationDetail> applicationDetails,
                               @JsonProperty("jurisdictionType") final JurisdictionType jurisdictionType
                               ) {
        this.hearingId = hearingId;
        this.vacatedTrialReasonId = vacatedTrialReasonId;
        this.code = code;
        this.description = description;
        this.type = type;
        this.courtCentreId = courtCentreId;
        this.hearingDay = hearingDay;
        this.caseDetails = caseDetails;
        this.hasInterpreter = hasInterpreter;
        this.applicationDetails = applicationDetails;
        this.jurisdictionType = jurisdictionType;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getVacatedTrialReasonId() {
        return vacatedTrialReasonId;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public void setVacatedTrialReasonId(final UUID vacatedTrialReasonId) {
        this.vacatedTrialReasonId = vacatedTrialReasonId;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setCourtCentreId(final UUID courtCentreId) {
        this.courtCentreId = courtCentreId;
    }

    public List<HearingCaseDetail> getCaseDetails() {
        return caseDetails;
    }

    public void setCaseDetails(final List<HearingCaseDetail> caseDetails) {
        this.caseDetails = caseDetails;
    }

    public ZonedDateTime getHearingDay() {
        return hearingDay;
    }

    public void setHearingDay(final ZonedDateTime hearingDay) {
        this.hearingDay = hearingDay;
    }

    public Boolean getHasInterpreter() {
        return hasInterpreter;
    }

    public void setHasInterpreter(final Boolean hasInterpreter) {
        this.hasInterpreter = hasInterpreter;
    }

    public List<HearingApplicationDetail> getApplicationDetails() {
        return applicationDetails;
    }

    public void setApplicationDetails(final List<HearingApplicationDetail> applicationDetails) {
        this.applicationDetails = applicationDetails;
    }

    public JurisdictionType getJurisdictionType() {
        return jurisdictionType;
    }

    public void setJurisdictionType(final JurisdictionType jurisdictionType) {
        this.jurisdictionType = jurisdictionType;
    }
}
