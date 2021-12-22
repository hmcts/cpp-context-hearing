package uk.gov.moj.cpp.hearing.persist.entity.ha;

import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.persist.entity.application.ApplicationDraftResult;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "ha_hearing")
public class Hearing {

    @Embedded
    private CourtCentre courtCentre;

    @Embedded
    private YouthCourt youthCourt;

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Embedded
    private HearingType hearingType;

    @Column(name = "jurisdiction_type")
    @Enumerated(EnumType.STRING)
    private JurisdictionType jurisdictionType;

    @Column(name = "reporting_restriction_reason")
    private String reportingRestrictionReason;

    @Column(name = "court_applications_json", columnDefinition = "TEXT")
    private String courtApplicationsJson;

    @Column(name = "hearing_language")
    @Enumerated(EnumType.STRING)
    private HearingLanguage hearingLanguage;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<HearingDay> hearingDays = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "hearing", orphanRemoval = true)
    private Set<ProsecutionCase> prosecutionCases = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "hearing", orphanRemoval = true)
    private Set<DefendantReferralReason> defendantReferralReasons = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "hearing", orphanRemoval = true)
    private Set<JudicialRole> judicialRoles = new HashSet<>();

    @Column(name = "has_shared_results")
    private Boolean hasSharedResults;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "hearing", orphanRemoval = true)
    private Set<Target> targets = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<ApplicationDraftResult> applicationDraftResults = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<DefendantAttendance> defendantAttendance = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<HearingCaseNote> hearingCaseNotes = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<HearingProsecutionCounsel> hearingProsecutionCounsels = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<HearingDefenceCounsel> hearingDefenceCounsels = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<HearingRespondentCounsel> hearingRespondentCounsels = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<HearingApplicantCounsel> hearingApplicantCounsels = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<HearingCompanyRepresentative> hearingCompanyRepresentatives = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<HearingInterpreterIntermediary> hearingInterpreterIntermediaries = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<HearingApplication> hearingApplications = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "hearing", orphanRemoval = true)
    private Set<ApprovalRequested> approvalsRequested = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "hearing", orphanRemoval = true)
    private Set<HearingYouthCourtDefendants> hearingYouthCourtDefendants = new HashSet<>();

    @Column(name = "trial_type_id")
    private UUID trialTypeId;

    @Column(name = "is_effective_trial")
    private Boolean isEffectiveTrial;

    @Column(name = "is_box_hearing")
    private Boolean isBoxHearing;

    @Column(name = "is_vacated_trial")
    private Boolean isVacatedTrial;

    @Column(name = "vacate_reason_id")
    private UUID vacatedTrialReasonId;

    @Column(name = "hearing_state")
    @Enumerated(EnumType.STRING)
    private HearingState hearingState;

    @Column(name = "amended_by_user_id")
    private UUID amendedByUserId;

    @Column(name = "earliest_next_hearing_date")
    private ZonedDateTime earliestNextHearingDate;

    @Column(name = "restrict_court_list_json", columnDefinition = "TEXT")
    private String restrictCourtListJson;

    public Hearing() {
        //For JPA
    }

    public CourtCentre getCourtCentre() {
        return courtCentre;
    }

    public Hearing setCourtCentre(CourtCentre courtCentre) {
        this.courtCentre = courtCentre;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public Hearing setId(UUID id) {
        this.id = id;
        return this;
    }

    public HearingType getHearingType() {
        return hearingType;
    }

    public Hearing setHearingType(HearingType hearingType) {
        this.hearingType = hearingType;
        return this;
    }

    public JurisdictionType getJurisdictionType() {
        return jurisdictionType;
    }

    public Hearing setJurisdictionType(JurisdictionType jurisdictionType2) {
        this.jurisdictionType = jurisdictionType2;
        return this;
    }

    public String getReportingRestrictionReason() {
        return reportingRestrictionReason;
    }

    public void setReportingRestrictionReason(String reportingRestrictionReason) {
        this.reportingRestrictionReason = reportingRestrictionReason;
    }

    public HearingLanguage getHearingLanguage() {
        return hearingLanguage;
    }

    public Hearing setHearingLanguage(HearingLanguage hearingLanguage2) {
        this.hearingLanguage = hearingLanguage2;
        return this;
    }

    public Set<HearingDay> getHearingDays() {
        return hearingDays;
    }

    public Hearing setHearingDays(Set<HearingDay> hearingDays) {
        this.hearingDays = hearingDays;
        return this;
    }

    public Set<ProsecutionCase> getProsecutionCases() {
        return prosecutionCases;
    }

    public Hearing setProsecutionCases(Set<ProsecutionCase> prosecutionCases) {
        this.prosecutionCases = prosecutionCases;
        return this;
    }

    public Set<DefendantReferralReason> getDefendantReferralReasons() {
        return defendantReferralReasons;
    }

    public Hearing setDefendantReferralReasons(Set<DefendantReferralReason> defendantReferralReasons) {
        this.defendantReferralReasons = defendantReferralReasons;
        return this;
    }

    public Set<JudicialRole> getJudicialRoles() {
        return judicialRoles;
    }

    public Hearing setJudicialRoles(Set<JudicialRole> judicialRoles) {
        this.judicialRoles = judicialRoles;
        return this;
    }

    public Boolean getHasSharedResults() {
        return hasSharedResults;
    }

    public Boolean getIsBoxHearing() {
        return isBoxHearing;
    }

    public Hearing setHasSharedResults(Boolean hasSharedResults) {
        this.hasSharedResults = hasSharedResults;
        return this;
    }

    public Hearing setIsBoxHearing(Boolean isBoxHearing) {
        this.isBoxHearing = isBoxHearing;
        return this;
    }

    public Set<Target> getTargets() {
        return targets;
    }


    public Hearing setTargets(Set<Target> targets) {
        this.targets = new HashSet<>(targets);
        return this;
    }

    public Set<ApplicationDraftResult> getApplicationDraftResults() {
        return applicationDraftResults;
    }

    public Hearing setApplicationDraftResults(final Set<ApplicationDraftResult> applicationDraftResults) {
        this.applicationDraftResults = applicationDraftResults;
        return this;
    }

    public String getCourtApplicationsJson() {
        return courtApplicationsJson;
    }


    public Hearing setCourtApplicationsJson(final String courtApplicationsJson) {
        this.courtApplicationsJson = courtApplicationsJson;
        return this;
    }

    public Set<HearingCaseNote> getHearingCaseNotes() {
        return hearingCaseNotes;
    }

    public Hearing setHearingCaseNotes(Set<HearingCaseNote> hearingCaseNotes) {
        this.hearingCaseNotes = hearingCaseNotes;
        return this;
    }


    public Set<DefendantAttendance> getDefendantAttendance() {
        return defendantAttendance;
    }

    public Hearing setDefendantAttendance(Set<DefendantAttendance> defendantAttendance) {
        this.defendantAttendance = defendantAttendance;
        return this;
    }

    public Set<HearingProsecutionCounsel> getProsecutionCounsels() {
        return hearingProsecutionCounsels;
    }

    public void setProsecutionCounsels(Set<HearingProsecutionCounsel> jpa) {
        this.hearingProsecutionCounsels = jpa;
    }

    public Set<HearingDefenceCounsel> getDefenceCounsels() {
        return hearingDefenceCounsels;
    }

    public void setDefenceCounsels(Set<HearingDefenceCounsel> jpa) {
        this.hearingDefenceCounsels = jpa;
    }

    public Set<HearingRespondentCounsel> getRespondentCounsels() {
        return hearingRespondentCounsels;
    }

    public void setRespondentCounsels(Set<HearingRespondentCounsel> jpa) {
        this.hearingRespondentCounsels = jpa;
    }

    public Set<HearingApplicantCounsel> getApplicantCounsels() {
        return hearingApplicantCounsels;
    }

    public void setApplicantCounsels(Set<HearingApplicantCounsel> jpa) {
        this.hearingApplicantCounsels = jpa;
    }

    public Set<HearingInterpreterIntermediary> getHearingInterpreterIntermediaries() {
        return hearingInterpreterIntermediaries;
    }

    public void setHearingInterpreterIntermediaries(Set<HearingInterpreterIntermediary> hearingInterpreterIntermediaries) {
        this.hearingInterpreterIntermediaries = hearingInterpreterIntermediaries;
    }

    public UUID getTrialTypeId() {
        return trialTypeId;
    }

    public Hearing setTrialTypeId(final UUID trialTypeId) {
        this.trialTypeId = trialTypeId;
        return this;
    }

    public Boolean getIsEffectiveTrial() {
        return isEffectiveTrial;
    }

    public Hearing setIsEffectiveTrial(Boolean effectiveTrial) {
        isEffectiveTrial = effectiveTrial;
        return this;
    }

    public Set<HearingCompanyRepresentative> getCompanyRepresentatives() {
        return hearingCompanyRepresentatives;
    }

    public void setCompanyRepresentatives(Set<HearingCompanyRepresentative> jpa) {
        this.hearingCompanyRepresentatives = jpa;
    }

    public Set<HearingApplication> getHearingApplications() {
        return hearingApplications;
    }

    public void setHearingApplications(final Set<HearingApplication> hearingApplications) {
        this.hearingApplications = hearingApplications;
    }

    public Boolean getIsVacatedTrial() {
        return isVacatedTrial;
    }

    public void setIsVacatedTrial(final Boolean vacatedTrial) {
        isVacatedTrial = vacatedTrial;
    }

    public UUID getVacatedTrialReasonId() {
        return vacatedTrialReasonId;
    }

    public void setvacatedTrialReasonId(final UUID vacatedTrialReasonId) {
        this.vacatedTrialReasonId = vacatedTrialReasonId;
    }

    @SuppressWarnings("squid:S2384")
    public Set<ApprovalRequested> getApprovalsRequested() {
        return approvalsRequested;
    }

    @SuppressWarnings("squid:S2384")
    public void setApprovalsRequested(Set<ApprovalRequested> approvalsRequested) {
        this.approvalsRequested = approvalsRequested;
    }

    public YouthCourt getYouthCourt() {
        return youthCourt;
    }

    public void setYouthCourt(YouthCourt youthCourt) {
        this.youthCourt = youthCourt;
    }

    public HearingState getHearingState() {
        return hearingState;
    }

    public void setHearingState(HearingState hearingState) {
        this.hearingState = hearingState;
    }

    public UUID getAmendedByUserId() {
        return amendedByUserId;
    }

    public void setAmendedByUserId(UUID amendedByUserId) {
        this.amendedByUserId = amendedByUserId;
    }

    public ZonedDateTime getEarliestNextHearingDate() {
        return earliestNextHearingDate;
    }

    public Hearing setEarliestNextHearingDate(final ZonedDateTime earliestNextHearingDate) {
        this.earliestNextHearingDate = earliestNextHearingDate;
        return this;
    }

    public String getRestrictCourtListJson() {
        return restrictCourtListJson;
    }

    public void setRestrictCourtListJson(String restrictCourtListJson) {
        this.restrictCourtListJson = restrictCourtListJson;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.id, ((Hearing) o).id);
    }
}