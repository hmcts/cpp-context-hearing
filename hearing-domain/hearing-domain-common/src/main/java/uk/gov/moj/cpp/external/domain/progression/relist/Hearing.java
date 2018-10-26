package uk.gov.moj.cpp.external.domain.progression.relist;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.json.schemas.core.HearingLanguage;
import uk.gov.justice.json.schemas.core.HearingType;
import uk.gov.justice.json.schemas.core.JurisdictionType;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(value = Include.NON_NULL)
public class Hearing implements Serializable {

    private static final long serialVersionUID = -7393868029752131025L;

    private HearingType type;
    private JurisdictionType jurisdictionType;
    private String reportingRestrictionReason;
    private HearingLanguage hearingLanguage;
    private LocalDateTime earliestStartDateTime;
    private int estimatedMinutes;
    private CourtCentre courtCentre;
    private List<JudicialRole> judiciary;
    private List<ProsecutionCase> prosecutionCases;

    public Hearing() {
    }

    @JsonCreator
    public Hearing(@JsonProperty(value = "type") final HearingType type,
                   @JsonProperty(value = "jurisdictionType") final JurisdictionType jurisdictionType,
                   @JsonProperty(value = "reportingRestrictionReason") final String reportingRestrictionReason,
                   @JsonProperty(value = "hearingLanguage") final HearingLanguage hearingLanguage,
                   @JsonProperty(value = "earliestStartDateTime") final LocalDateTime earliestStartDateTime,
                   @JsonProperty(value = "estimatedMinutes") final int estimatedMinutes,
                   @JsonProperty(value = "courtCentre") final CourtCentre courtCentre,
                   @JsonProperty(value = "judiciary") final List<JudicialRole> judiciary,
                   @JsonProperty(value = "prosecutionCases") final List<ProsecutionCase> prosecutionCases) {
        this.type = type;
        this.jurisdictionType = jurisdictionType;
        this.reportingRestrictionReason = reportingRestrictionReason;
        this.hearingLanguage = hearingLanguage;
        this.earliestStartDateTime = earliestStartDateTime;
        this.estimatedMinutes = estimatedMinutes;
        this.courtCentre = courtCentre;
        this.judiciary = judiciary;
        this.prosecutionCases = prosecutionCases;
    }

    public HearingType getType() {
        return type;
    }

    public Hearing setType(HearingType type) {
        this.type = type;
        return this;
    }

    public JurisdictionType getJurisdictionType() {
        return jurisdictionType;
    }

    public Hearing setJurisdictionType(JurisdictionType jurisdictionType) {
        this.jurisdictionType = jurisdictionType;
        return this;
    }

    public String getReportingRestrictionReason() {
        return reportingRestrictionReason;
    }

    public Hearing setReportingRestrictionReason(String reportingRestrictionReason) {
        this.reportingRestrictionReason = reportingRestrictionReason;
        return this;
    }

    public HearingLanguage getHearingLanguage() {
        return hearingLanguage;
    }

    public Hearing setHearingLanguage(HearingLanguage hearingLanguage) {
        this.hearingLanguage = hearingLanguage;
        return this;
    }

    public LocalDateTime getEarliestStartDateTime() {
        return earliestStartDateTime;
    }

    public Hearing setEarliestStartDateTime(LocalDateTime earliestStartDateTime) {
        this.earliestStartDateTime = earliestStartDateTime;
        return this;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public Hearing setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
        return this;
    }

    public CourtCentre getCourtCentre() {
        return courtCentre;
    }

    public Hearing setCourtCentre(CourtCentre courtCentre) {
        this.courtCentre = courtCentre;
        return this;
    }

    public List<JudicialRole> getJudiciary() {
        return judiciary;
    }

    public Hearing setJudiciary(List<JudicialRole> judiciary) {
        this.judiciary = judiciary;
        return this;
    }

    public List<ProsecutionCase> getProsecutionCases() {
        return prosecutionCases;
    }

    public Hearing setProsecutionCases(List<ProsecutionCase> prosecutionCases) {
        this.prosecutionCases = prosecutionCases;
        return this;
    }

    public static Hearing hearing() {
        return new Hearing();
    }
}
